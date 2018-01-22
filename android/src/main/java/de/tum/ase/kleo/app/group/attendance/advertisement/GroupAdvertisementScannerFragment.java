package de.tum.ase.kleo.app.group.attendance.advertisement;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.client.dto.SessionDTO;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.Advertisement;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.AdvertisementScanner;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.HandshakeClient;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.HandshakeServer;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GroupAdvertisementScannerFragment extends ReactiveLayoutFragment {

    private AdvertisementScanner adScanner;
    private GroupsApi groupsApi;
    private HandshakeClient handshakeClient;
    private String currentUserId;

    public GroupAdvertisementScannerFragment() {
        super(R.layout.fragment_group_advertisement_scanner);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        currentUserId = backendClient.principal().blockingGet().id();

        groupsApi = backendClient.as(GroupsApi.class);

        adScanner = AdvertisementScanner.createDefault(HandshakeServer.SERVICE_UUID);
        handshakeClient = HandshakeClient.create(getContext(), HandshakeServer.SERVICE_UUID);
    }

    @Override
    protected void onCreateLayout(View view, LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        final Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.breath);
        view.findViewById(R.id.radarIcon).startAnimation(shake);

        final RecyclerView listView = view.findViewById(R.id.group_ad_scanner_list);
        listView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        final GroupAdvertisementScannerAdapter adapter = new GroupAdvertisementScannerAdapter();
        listView.setAdapter(adapter);

        adapter.setGroupSessionOnClickListener((blDevice, group, sessionId) -> {
            handshakeClient.requestHandshake(blDevice, currentUserId, group.getId(), sessionId.getId())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((passCode) -> {
                        groupsApi.utilizeSessionPass(group.getId(), passCode)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    Toast.makeText(getContext(), "Nice to have you! Your attendance has been recorded!", Toast.LENGTH_SHORT).show();
                                });
                    });
        });

        adScanner.scanUnique()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceAdv -> {
                    final BluetoothDevice device = deviceAdv.first;
                    final Advertisement advertisement = deviceAdv.second;

                    Disposable disposable = groupsApi.getGroup(advertisement.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(group -> adapter.appendAdvertisement(Pair.create(device, group)),
                                    this::showError);

                    disposeOnDestroy(disposable);
                });
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adScanner.stop();
    }
}
