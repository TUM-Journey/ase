package de.tum.ase.kleo.app.group.advertisement;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import static de.tum.ase.kleo.app.group.advertisement.GroupAdvertisement.MESSAGE_CHARSET;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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
    private ObservableEmitter<GroupAdvertisement> emmiter;

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

    public Observable<GroupAdvertisement> scan() {
        return Observable.create(emmiter -> {
            this.emmiter = emmiter;
            this.scanCallback = createScanCallback(emmiter);

            bluetoothLeScanner.startScan(emptyList(), scanSettings, scanCallback);
        });
    }

    private ScanCallback createScanCallback(ObservableEmitter<GroupAdvertisement> emmiter) {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                if (result == null)
                    return;

                final BluetoothDevice sender = result.getDevice();
                if (sender == null)
                    return;

                final ScanRecord scanRecord = result.getScanRecord();
                final byte[] serviceData = scanRecord.getServiceData(uuid);

                if (serviceData == null)
                    return;

                final String messageText = new String(serviceData, MESSAGE_CHARSET);
                emmiter.onNext(new GroupAdvertisement(sender, messageText));
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
        this.emmiter.onComplete();
    }
}
