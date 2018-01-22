package de.tum.ase.kleo.app.group.attendance.advertisement.handshake;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY;
import static java.lang.String.format;

public class AdvertisementScanner {

    private static final String TAG = AdvertisementScanner.class.getSimpleName();

    private final UUID serviceId;
    private final ScanSettings scanSettings;

    private final HashSet<BluetoothDevice> knownDevices = new HashSet<>();
    private BluetoothLeScanner bluetoothLeScanner;

    private ScanCallback scanCallback;
    private ObservableEmitter<Pair<BluetoothDevice, Advertisement>> scanStreamEmitter;

    private AdvertisementScanner(UUID serviceId, BluetoothLeScanner bluetoothLeScanner, ScanSettings scanSettings) {
        this.bluetoothLeScanner = bluetoothLeScanner;
        this.serviceId = serviceId;
        this.scanSettings = scanSettings;
    }

    public static AdvertisementScanner create(UUID serviceId, int scanMode) {
        final BluetoothLeScanner bleScanner
                = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

        final ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(scanMode)
                .build();

        return new AdvertisementScanner(serviceId, bleScanner, scanSettings);
    }

    public static AdvertisementScanner createDefault(UUID serviceId) {
        return create(serviceId, SCAN_MODE_LOW_LATENCY);
    }

    private Observable<Pair<BluetoothDevice, Advertisement>> scan(boolean onlyOnceFromDevice) {
        return Observable.create(emitter -> {
            scanStreamEmitter = emitter;

            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (result == null)
                        return;

                    final BluetoothDevice sender = result.getDevice();
                    if (sender == null)
                        return;

                    if (knownDevices.contains(sender) && onlyOnceFromDevice)
                        return;

                    final ScanRecord scanRecord = result.getScanRecord();
                    final byte[] serviceData = scanRecord.getServiceData(new ParcelUuid(serviceId));

                    if (serviceData == null) {
                        return;
                    }

                    knownDevices.add(sender);

                    final Advertisement advertisement = Advertisement.fromBytes(serviceData);
                    Log.d(TAG, format("Advertisement arrived %s from %s",
                            advertisement.toString(), sender.getAddress()));

                    emitter.onNext(Pair.create(sender, advertisement));
                }

                @Override
                public void onScanFailed(int errorCode) {
                    emitter.onError(new RuntimeException("Failed to scan ad, code = " + errorCode));
                }
            };

            bluetoothLeScanner.startScan(Collections.emptyList(), scanSettings, scanCallback);
        });
    }

    public Observable<Pair<BluetoothDevice, Advertisement>> scanUnique() {
        return scan(true);
    }

    public Observable<Pair<BluetoothDevice, Advertisement>> scanAmb() {
        return scan(false);
    }

    public void stop() {
        if (scanCallback != null) {
            bluetoothLeScanner.stopScan(scanCallback);

            if (scanStreamEmitter != null) {
                scanStreamEmitter.onComplete();
            }
        }
    }

    public Set<BluetoothDevice> knownDevices() {
        return Collections.unmodifiableSet(knownDevices);
    }

    public void clearKnownDevices() {
        knownDevices.clear();
    }
}
