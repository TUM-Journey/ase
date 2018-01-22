package de.tum.ase.kleo.app.group.attendance.advertisement.handshake;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Completable;

import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY;
import static android.content.Context.BLUETOOTH_SERVICE;
import static java.lang.String.format;

public class HandshakeServer {

    public static final UUID SERVICE_UUID = UUID.fromString("7CFC44E2-26BE-4932-8304-C5C68A1C66D2");

    private static final String TAG = HandshakeServer.class.getSimpleName();

    private static final BluetoothGattService gattService = new BluetoothGattService(SERVICE_UUID,
            SERVICE_TYPE_PRIMARY) {{
        addCharacteristic(HandshakeRequest.characteristic());
        addCharacteristic(HandshakeResponse.characteristic());
    }};

    private final HandshakeStorage handshakeStorage = new HandshakeStorage();
    private final Context context;
    private final BluetoothManager bluetoothManager;
    private final HandshakeSupplier handshakeSupplier;

    private BluetoothGattServer gattServer;

    private HandshakeServer(Context ctx, BluetoothManager blMgr, HandshakeSupplier handshakeSupplier) {
        this.context = ctx;
        this.bluetoothManager = blMgr;
        this.handshakeSupplier = handshakeSupplier;
    }

    public static HandshakeServer create(Context ctx, HandshakeSupplier handshakeSupplier) {
        final BluetoothManager blMgr = (BluetoothManager) ctx.getSystemService(BLUETOOTH_SERVICE);
        return new HandshakeServer(ctx, blMgr, handshakeSupplier);
    }

    public Completable listen() {
        return Completable.create(emitter -> {
            gattServer = bluetoothManager.openGattServer(context, new BluetoothGattServerCallback() {
                @Override
                public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "BluetoothDevice " + device.getAddress() + "CONNECTED");
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(TAG, "BluetoothDevice " + device.getAddress() + "DISCONNECTED");
                    }
                }

                @Override
                public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                                        int offset, BluetoothGattCharacteristic chr) {
                    Log.i(TAG, "onCharacteristicReadRequest: " + device.getAddress()
                            + ", characteristic = " + chr.getUuid());

                    if (!HandshakeResponse.CHAR_UUID.equals(chr.getUuid())) {
                        Log.i(TAG, "onCharacteristicReadRequest: " +
                                "Unknown BluetoothGattCharacteristic uuid " + chr.getUuid());
                        return;
                    }

                    if (!handshakeStorage.hasHandshake(device)) {
                        Log.w(TAG, "onCharacteristicReadRequest: " +
                                "Device tried to access a handshake without handshake request");
                        gattServer.sendResponse(device, requestId, GATT_FAILURE, 0, null);
                        return;
                    }

                    final String handshake = handshakeStorage.getHandshake(device);
                    final HandshakeResponse handshakeRes = HandshakeResponse.from(handshake);
                    gattServer.sendResponse(device, requestId, GATT_SUCCESS, 0, handshakeRes.toBytes());

                    Log.i(TAG, "onCharacteristicReadRequest: " +
                            format("Handshake %s has been sent to device %s",
                                    handshake, device.getAddress()));
                }

                @Override
                public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                         BluetoothGattCharacteristic chr,
                                                         boolean prepWrite, boolean responseNeeded,
                                                         int offset, byte[] value) {
                    Log.i(TAG, "onCharacteristicWriteRequest: " + device.getAddress()
                            + ", characteristic = " + chr.getUuid() + ", responseNeeded = "
                            + responseNeeded + ", offset = " + offset
                            + ", value = " + new String(value) + " ~ " + value.length);

                    if (!HandshakeRequest.CHAR_UUID.equals(chr.getUuid())) {
                        Log.i(TAG, "onCharacteristicWriteRequest: " +
                                "Unknown BluetoothGattCharacteristic uuid " + chr.getUuid());
                        return;
                    }

                    final HandshakeRequest handshakeReq = HandshakeRequest.fromBytes(value);
                    final String handshake = handshakeSupplier.supply(handshakeReq.studentId(),
                            handshakeReq.groupIdOrCode(),
                            handshakeReq.sessionId());

                    handshakeStorage.storeHandshake(device, handshake);

                    Log.i(TAG, "onCharacteristicWriteRequest: " +
                            format("Handshake %s has been created and saved for device %s",
                                    handshake, device.getAddress()));

                    if (responseNeeded) {
                        gattServer.sendResponse(device, requestId, GATT_SUCCESS, 0, null);
                        Log.i(TAG, "onCharacteristicWriteRequest: Device notified with GATT_SUCCESS");
                    }
                }

                @Override
                public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                                                    int offset, BluetoothGattDescriptor descr) {
                    Log.i(TAG, "onDescriptorReadRequest: " + device.getAddress()
                            + ", descriptor = " + descr.getUuid());
                }

                @Override
                public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                                     BluetoothGattDescriptor descriptor,
                                                     boolean preparedWrite, boolean responseNeeded,
                                                     int offset, byte[] value) {
                    Log.i(TAG, "onDescriptorWriteRequest: " + device.getAddress()
                            + ", descriptor = " + descriptor.getUuid());
                }

                @Override
                public void onNotificationSent(BluetoothDevice device, int status) {
                    Log.i(TAG, "onNotificationSent: " + device.getAddress()
                            + ", status = " + status);
                }

                @Override
                public void onServiceAdded(int status, BluetoothGattService service) {
                    emitter.onComplete();
                }

                @Override
                public void onMtuChanged(BluetoothDevice device, int mtu) {
                    Log.i(TAG, "onMtuChanged: " + device.getAddress()
                            + ", new mtu = " + mtu);
                }
            });

            gattServer.addService(gattService);
        });
    }

    public void stop() {
        if (gattServer != null) {
            gattServer.close();
        }
    }

    private static class HandshakeStorage {

        private final Map<BluetoothDevice, String> deviceHandshakes = new HashMap<>();

        public boolean hasHandshake(BluetoothDevice device) {
            return deviceHandshakes.containsKey(device);
        }

        public void storeHandshake(BluetoothDevice device, String handshake) {
            deviceHandshakes.put(device, handshake);
        }

        public String getHandshake(BluetoothDevice device) {
            return deviceHandshakes.get(device);
        }
    }

    public interface HandshakeSupplier {
        String supply(String studentId, String groupIdOrCode, String sessionId);
    }
}
