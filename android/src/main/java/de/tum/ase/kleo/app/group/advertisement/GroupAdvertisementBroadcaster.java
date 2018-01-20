package de.tum.ase.kleo.app.group.advertisement;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class GroupAdvertisementBroadcaster {

    private static final AdvertiseSettings defaultAdvertiseSettings
            = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build();
    private static final AdvertiseCallback defaultAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            throw new RuntimeException("Failed to start advertising. Error code: " + errorCode);
        }
    };

    private final ParcelUuid uuid;
    private final BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private final AdvertiseCallback advertiseCallback;
    private final AdvertiseSettings advertiseSettings;

    public GroupAdvertisementBroadcaster(String uuid) {
        this(ParcelUuid.fromString(uuid), null, null);
    }

    public GroupAdvertisementBroadcaster(ParcelUuid uuid) {
        this(uuid, null, null);
    }

    public GroupAdvertisementBroadcaster(ParcelUuid uuid, AdvertiseCallback advertiseCallback, AdvertiseSettings advertiseSettings) {
        this.uuid = uuid;
        this.bluetoothLeAdvertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        this.advertiseCallback = defaultIfNull(advertiseCallback, defaultAdvertiseCallback);
        this.advertiseSettings = defaultIfNull(advertiseSettings, defaultAdvertiseSettings);
    }

    public void advertise() {
        final AdvertiseData adData = new AdvertiseData.Builder()
                .addServiceUuid(uuid)
                .build();

        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, adData, advertiseCallback);
    }

    public void stopAdvertising() {
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
    }
}
