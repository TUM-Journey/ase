package de.tum.ase.kleo.app.group.attendance.advertisement.handshake;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.UUID;

import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY;
import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;

public class AdvertisementBroadcaster {
    private static final String TAG = AdvertisementBroadcaster.class.getSimpleName();

    private final BluetoothLeAdvertiser broadcaster = BluetoothAdapter.getDefaultAdapter()
            .getBluetoothLeAdvertiser();

    private final UUID serviceId;
    private final AdvertiseCallback advertiseCallback;
    private final AdvertiseSettings advertiseSettings;

    private AdvertisementBroadcaster(UUID serviceId, int advertiseMode, int txPowerLevel,
                                     AdvertiseCallback advertiseCallback) {
        this.serviceId = serviceId;
        this.advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(advertiseMode)
                .setTxPowerLevel(txPowerLevel)
                .setConnectable(true)
                .build();

        this.advertiseCallback =
                advertiseCallback == null ? defaultAdvertiseCallback : advertiseCallback;
    }

    public static AdvertisementBroadcaster create(UUID serviceId, int advertiseMode,
                                                  int txPowerLevel, AdvertiseCallback adCallback) {
        return new AdvertisementBroadcaster(serviceId, advertiseMode, txPowerLevel, adCallback);
    }

    public static AdvertisementBroadcaster createDefault(UUID serviceId) {
        return new AdvertisementBroadcaster(serviceId,
                ADVERTISE_MODE_LOW_LATENCY, ADVERTISE_TX_POWER_HIGH, null);
    }

    public void broadcast(Advertisement advertisement) {
        final AdvertiseData adData = new AdvertiseData.Builder()
                .addServiceData(new ParcelUuid(serviceId), advertisement.toBytes())
                .build();

        broadcaster.startAdvertising(advertiseSettings, adData, advertiseCallback);
    }

    public void stop() {
        broadcaster.stopAdvertising(advertiseCallback);
    }

    private static final AdvertiseCallback defaultAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "AdvertisementBroadcaster started successfully");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.d(TAG, "AdvertisementBroadcaster start failed");
        }
    };
}
