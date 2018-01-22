package de.tum.ase.kleo.app.group.attendance.advertisement.handshake;

import java.nio.charset.Charset;

public class Advertisement {

    public static final int MAX_BYTES = 15;
    private static final Charset ENCODING = Charset.forName("US-ASCII");

    private final byte[] content;

    private Advertisement(byte[] content) {
        this.content = content;
    }

    public static Advertisement from(String text) {
        final byte[] textBytes = text.getBytes(ENCODING);
        if (textBytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("Advertisement is too big. Allowed <= " + MAX_BYTES);
        }
        return new Advertisement(textBytes);
    }

    public static Advertisement fromBytes(byte[] bytes) {
        return new Advertisement(bytes);
    }

    @Override
    public String toString() {
        return new String(content, ENCODING);
    }

    public byte[] toBytes() {
        return content;
    }
}
