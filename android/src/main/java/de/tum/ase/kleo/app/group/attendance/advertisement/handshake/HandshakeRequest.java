package de.tum.ase.kleo.app.group.attendance.advertisement.handshake;

import android.bluetooth.BluetoothGattCharacteristic;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;

public class HandshakeRequest {

    private static final Charset ENCODING = Charset.forName("UTF-8");
    public static final int MAX_BYTES = 128;
    public static final UUID CHAR_UUID = UUID.fromString("97C4FF95-F41A-477B-8CE9-BA95714D7060");

    private final String studentId;
    private final String groupIdOrCode;
    private final String sessionId;

    public HandshakeRequest(String studentId, String groupIdOrCode, String sessionId) {
        this.studentId = studentId;
        this.groupIdOrCode = groupIdOrCode;
        this.sessionId = sessionId;
    }

    public String studentId() {
        return studentId;
    }

    public String groupIdOrCode() {
        return groupIdOrCode;
    }

    public String sessionId() {
        return sessionId;
    }

    public byte[] toBytes() {
        final byte[] studentIdBytes = studentId.getBytes(ENCODING);
        final byte[] groupIdOrCodeBytes = groupIdOrCode.getBytes(ENCODING);
        final byte[] sessionIdBytes = sessionId.getBytes(ENCODING);

        final ByteBuffer handshakeReqBuffer = ByteBuffer.allocate(
                Integer.BYTES + studentIdBytes.length +
                        Integer.BYTES + groupIdOrCodeBytes.length +
                        Integer.BYTES + sessionIdBytes.length);

        handshakeReqBuffer
                .putInt(studentIdBytes.length).put(studentIdBytes)
                .putInt(groupIdOrCodeBytes.length).put(groupIdOrCodeBytes)
                .putInt(sessionIdBytes.length).put(sessionIdBytes);

        final byte[] handshakeReqBytes = new byte[handshakeReqBuffer.capacity()];
        handshakeReqBuffer.clear();
        handshakeReqBuffer.get(handshakeReqBytes);

        if (handshakeReqBytes.length > MAX_BYTES) {
            throw new IllegalStateException("Handshake request is too big, check ids passed");
        }

        return handshakeReqBytes;
    }

    public static HandshakeRequest fromBytes(byte[] bytes) {
        final ByteBuffer handshakeReqBuffer = ByteBuffer.wrap(bytes);

        byte[] studentIdBytes = new byte[handshakeReqBuffer.getInt()];
        handshakeReqBuffer.get(studentIdBytes);
        String studentId = new String(studentIdBytes, ENCODING);

        byte[] groupIdOrCodeBytes = new byte[handshakeReqBuffer.getInt()];
        handshakeReqBuffer.get(groupIdOrCodeBytes);
        String groupIdOrCode = new String(groupIdOrCodeBytes, ENCODING);

        byte[] sessionIdBytes = new byte[handshakeReqBuffer.getInt()];
        handshakeReqBuffer.get(sessionIdBytes);
        String sessionId = new String(sessionIdBytes, ENCODING);

        return new HandshakeRequest(studentId, groupIdOrCode, sessionId);
    }

    public static BluetoothGattCharacteristic characteristic() {
        return new BluetoothGattCharacteristic(CHAR_UUID, PROPERTY_WRITE, PERMISSION_WRITE);
    }
}
