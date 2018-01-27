package de.tum.ase.kleo.app.group.attendance.advertisement;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v13.app.FragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.Advertisement;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.AdvertisementScanner;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.HandshakeClient;
import de.tum.ase.kleo.app.group.attendance.advertisement.handshake.HandshakeServer;
import de.tum.ase.kleo.app.support.ReactiveLayoutFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.content.Context.BLUETOOTH_SERVICE;
import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeIn;
import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeOut;

public class GroupAdvertisementScannerFragment extends ReactiveLayoutFragment
        implements FragmentCompat.OnRequestPermissionsResultCallback  {

    private static final int REQ_COARSE_LOC_PERM = 1;
    private static final int REQ_BLUETOOTH_ENABLE = 2;

    private AdvertisementScanner adScanner;
    private GroupsApi groupsApi;
    private HandshakeClient handshakeClient;
    private String currentUserId;
    private RecyclerView listView;
    private ToggleButton scanToggle;
    private ProgressBar progressBar;

    public GroupAdvertisementScannerFragment() {
        super(R.layout.fragment_group_advertisement_scanner);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        currentUserId = backendClient.principal().id();

        groupsApi = backendClient.as(GroupsApi.class);

        adScanner = AdvertisementScanner.createDefault(HandshakeServer.SERVICE_UUID);
        handshakeClient = HandshakeClient.create(getContext(), HandshakeServer.SERVICE_UUID);
    }

    @Override
    protected void onFragmentCreated(View view, Bundle state) {
        progressBar = view.findViewById(R.id.group_ad_scanner_progressbar);
        scanToggle = view.findViewById(R.id.group_ad_scanner_toggle);
        listView = view.findViewById(R.id.group_ad_scanner_list_view);
        listView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        scanToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!hasBluetoothEnabled() || !hasLocationPermissions()) {
                requestBluetoothEnable();
                requestLocationPermission();
                buttonView.setChecked(false);
            } else {
                if (isChecked) {
                    startScanning();
                } else {
                    stopScanning();
                }
            }
        });
    }

    private boolean hasLocationPermissions() {
        return checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasBluetoothEnabled() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getContext().getSystemService(BLUETOOTH_SERVICE);

        return bluetoothManager.getAdapter().isEnabled();
    }

    private void requestLocationPermission() {
        requestPermissions(new String[] {ACCESS_COARSE_LOCATION}, REQ_COARSE_LOC_PERM);
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQ_BLUETOOTH_ENABLE);
    }

    private void startScanning() {
        final GroupAdvertisementScannerAdapter adapter = setupGroupAdvertisementList();
        adScanner.scanUnique()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceAdv -> {
                    final BluetoothDevice device = deviceAdv.first;
                    final Advertisement advertisement = deviceAdv.second;

                    Disposable disposable = groupsApi.getGroup(advertisement.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(group -> {
                                final List<String> registeredStudentIds = group.getStudentIds();
                                boolean isIRegistered = registeredStudentIds != null
                                        && registeredStudentIds.contains(currentUserId);

                                if (isIRegistered) {
                                    adapter.appendActiveAdvertisement(device, group);
                                } else {
                                    adapter.appendInactiveAdvertisement(device, group);
                                }
                            }, this::showError);

                    disposeOnDestroy(disposable);
                });
    }

    private GroupAdvertisementScannerAdapter setupGroupAdvertisementList() {
        final GroupAdvertisementScannerAdapter adapter = new GroupAdvertisementScannerAdapter();
        adapter.setGroupSessionOnClickListener((blDevice, group, sessionId) -> {
            handshakeClient.requestHandshake(blDevice, currentUserId, group.getId(), sessionId.getId())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(r -> this.showProgressBar())
                    .doOnError(e -> this.hideProgressBar())
                    .subscribe((passCode) -> {
                        groupsApi.utilizeSessionPass(group.getId(), passCode)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doFinally(this::hideProgressBar)
                                .subscribe(() -> {
                                    Toast.makeText(getContext(), R.string.group_ad_scanner_item_joined_welcome_notice, Toast.LENGTH_SHORT).show();
                                });
                    });
        });

        listView.setAdapter(adapter);
        return adapter;
    }

    private void stopScanning() {
        adScanner.stop();
        listView.setAdapter(null);
    }

    protected void showProgressBar() {
        fadeIn(progressBar);
    }

    protected void hideProgressBar() {
        fadeOut(progressBar);
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScanning();
    }
}
