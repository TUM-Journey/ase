package de.tum.ase.kleo.app.group.attendance.advertisement;

import android.bluetooth.BluetoothDevice;

import java.nio.charset.Charset;

public class Advertisement {

    public static final Charset MESSAGE_CHARSET = Charset.forName("US-ASCII");

    private final BluetoothDevice sender;
    private final String message;

    public Advertisement(BluetoothDevice sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public BluetoothDevice sender() {
        return sender;
    }

    public String message() {
        return message;
    }
}
