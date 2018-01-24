package de.tum.ase.kleo.app.group.attendance.advertisement.handshake;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.reactivex.Single;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static java.lang.Math.max;

public class HandshakeClient {

    private static final String TAG = HandshakeClient.class.getSimpleName();

    private static final int MTU = max(HandshakeRequest.MAX_BYTES, HandshakeResponse.MAX_BYTES);

    private final UUID serviceId;
    private final Context context;

    private HandshakeClient(Context context, UUID serviceId) {
        this.serviceId = serviceId;
        this.context = context;
    }

    public static HandshakeClient create(Context ctx, UUID serviceId) {
        return new HandshakeClient(ctx, serviceId);
    }

    public Single<String> requestHandshake(BluetoothDevice device, String studentId,
                                           String groupIdOrCode, String sessionId) {

        final CompletableFuture<Void> connectionConfigured = new CompletableFuture<>();

        final CompletableFuture<Void> handshakeReqSent = new CompletableFuture<>();
        final CompletableFuture<Void> handshakeResReceived = new CompletableFuture<>();

        final BluetoothGatt bluetoothGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "BluetoothDevice connected to " + device.getAddress());
                    gatt.requestMtu(MTU);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.d(TAG, device.getAddress() + "'s services discovered");
                connectionConfigured.complete(null);
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                Log.d(TAG, " MTU with device " + device.getAddress()
                        + " has been set to " + mtu);
                gatt.discoverServices();
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic c, int status) {
                Log.d(TAG, "onCharacteristicWrite: c.value()= " + new String(c.getValue()));
                if (HandshakeRequest.CHAR_UUID.equals(c.getUuid())) {
                    if (status == GATT_SUCCESS) {
                        handshakeReqSent.complete(null);
                    } else {
                        handshakeReqSent.completeExceptionally
                                (new IllegalStateException("Failed to request handshake. " +
                                        "Gatt answer status = " + status));
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic c) {
                Log.d(TAG, "onCharacteristicChanged: c.value()= " + new String(c.getValue()));
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic c, int status) {
                Log.d(TAG, "onCharacteristicRead: c.value()= " + new String(c.getValue()));

                if (HandshakeResponse.CHAR_UUID.equals(c.getUuid())) {
                    if (status == GATT_SUCCESS) {
                        handshakeResReceived.complete(null);
                    } else {
                        handshakeReqSent.completeExceptionally
                                (new IllegalStateException("Failed to receive handshake. " +
                                        "Gatt answer status = " + status));
                    }
                }
            }
        });

        return Single.fromFuture(connectionConfigured.thenRun(() -> {
            Log.i(TAG, "Handshake client configured. Retrieving handshake...");
            final HandshakeRequest handshakeReq
                    = new HandshakeRequest(studentId, groupIdOrCode, sessionId);

            final BluetoothGattCharacteristic remoteHandshakeReqCharacteristic
                    = bluetoothGatt.getService(serviceId)
                    .getCharacteristic(HandshakeRequest.CHAR_UUID);

            remoteHandshakeReqCharacteristic.setValue(handshakeReq.toBytes());
            bluetoothGatt.writeCharacteristic(remoteHandshakeReqCharacteristic);
        }).thenCombine(handshakeReqSent, (na, na2) -> {
            final BluetoothGattCharacteristic remoteHandshakeResCharacteristic
                    = bluetoothGatt.getService(serviceId)
                    .getCharacteristic(HandshakeResponse.CHAR_UUID);
            bluetoothGatt.readCharacteristic(remoteHandshakeResCharacteristic);

            return remoteHandshakeResCharacteristic;
        }).thenCombine(handshakeResReceived, (handshakeResCharacteristic, na) -> {
            final HandshakeResponse handshakeRes
                    = HandshakeResponse.fromBytes(handshakeResCharacteristic.getValue());

            return handshakeRes.toString();
        })).doFinally(bluetoothGatt::close);
    }
}
