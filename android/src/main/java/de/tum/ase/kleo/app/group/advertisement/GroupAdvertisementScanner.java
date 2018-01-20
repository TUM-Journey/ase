package de.tum.ase.kleo.app.group.advertisement;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class GroupAdvertisementScanner {

    private final static ScanSettings defaultScanSettings
            = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

    private final ParcelUuid uuid;
    private final BluetoothLeScanner bluetoothLeScanner;
    private final ScanSettings scanSettings;

    private ScanCallback scanCallback;

    public GroupAdvertisementScanner(String uuid) {
        this(ParcelUuid.fromString(uuid));
    }

    public GroupAdvertisementScanner(ParcelUuid uuid) {
        this(uuid, null);
    }

    public GroupAdvertisementScanner(ParcelUuid uuid, ScanSettings scanSettings) {
        this.uuid = uuid;
        this.scanSettings = defaultIfNull(scanSettings, defaultScanSettings);
        this.bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    }

    public Observable<BluetoothDevice> scan() {
        final List<ScanFilter> scanFilters = asList(createUuidFilter(uuid));

        return Observable.create(emmiter -> {
            scanCallback = createScanCallback(emmiter);
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        });
    }

    private ScanCallback createScanCallback(ObservableEmitter<BluetoothDevice> emmiter) {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                if (result == null)
                    return;

                final BluetoothDevice device = result.getDevice();
                if (device == null)
                    return;

                emmiter.onNext(device);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                emmiter.onError(new RuntimeException("BLE Scan failed. Code = " + errorCode));
            }
        };
    }

    public void stopScanning() {
        bluetoothLeScanner.stopScan(scanCallback);
    }

    private static ScanFilter createUuidFilter(ParcelUuid parcelUuid) {
        return new ScanFilter.Builder()
                .setServiceUuid(parcelUuid)
                .build();
    }
}
