package de.tum.ase.kleo.app.group.attendance.advertisement;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import de.tum.ase.kleo.android.BuildConfig;
import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GroupAdvertisementScannerFragment extends ReactiveLayoutFragment {

    private AdvertisementScanner adScanner;
    private GroupsApi groupsApi;

    public GroupAdvertisementScannerFragment() {
        super(R.layout.fragment_group_advertisement_scanner);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        groupsApi = backendClient.as(GroupsApi.class);

        adScanner = new AdvertisementScanner(BuildConfig.BLUETOOTH_PARCEL_ID);
    }

    @Override
    protected void onCreateLayout(View view, LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        final Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.breath);
        view.findViewById(R.id.radarIcon).startAnimation(shake);

        final RecyclerView listView = view.findViewById(R.id.group_ad_scanner_list);
        listView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        final GroupAdvertisementScannerAdapter adapter = new GroupAdvertisementScannerAdapter();
        listView.setAdapter(adapter);

        adScanner.scan()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ad -> !adapter.hasGroup(ad.message()))
                .subscribe(ad -> {
                    final String groupCode = ad.message();

                    Disposable disposable = groupsApi.getGroup(groupCode)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(adapter::appendGroup, this::showError);

                    disposeOnDestroy(disposable);
                });
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        adScanner.stopScanning();
    }
}
