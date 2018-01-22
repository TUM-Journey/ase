package de.tum.ase.kleo.app.group.attendance.advertisement.handshake;

import android.bluetooth.BluetoothGattCharacteristic;

import java.nio.charset.Charset;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;

public class HandshakeResponse {

    private static final Charset ENCODING = Charset.forName("UTF-8");
    public static final int MAX_BYTES = 248;
    public static final UUID CHAR_UUID = UUID.fromString("2E65B0EF-4D5D-4878-9534-765A7702847B");

    private final byte[] handshake;

    private HandshakeResponse(byte[] handshake) {
        this.handshake = handshake;
    }

    public static HandshakeResponse from(String handshake) {
        final byte[] handshakeBytes = handshake.getBytes(ENCODING);
        if (handshakeBytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("Handshake size is too big. Max allowed = " + MAX_BYTES);
        }

        return new HandshakeResponse(handshakeBytes);
    }

    public static HandshakeResponse fromBytes(byte[] bytes) {
        return new HandshakeResponse(bytes);
    }

    public byte[] toBytes() {
        return handshake;
    }

    @Override
    public String toString() {
        return new String(handshake, ENCODING);
    }

    public static BluetoothGattCharacteristic characteristic() {
        return new BluetoothGattCharacteristic(CHAR_UUID, PROPERTY_READ, PERMISSION_READ);
    }
}
