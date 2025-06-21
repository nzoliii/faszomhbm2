package com.hbm.util;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class BufferUtil {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    // Writes a string to a byte buffer by encoding the length and raw bytes
    public static final void writeString(ByteBuf buf, String value) {
        if(value == null) {
            buf.writeInt(-1);
            return;
        }

        buf.writeInt(value.length());
        buf.writeBytes(value.getBytes(CHARSET));
    }

    // Reads a string from a byte buffer via the written length and raw bytes
    public static final String readString(ByteBuf buf) {
        final int count = buf.readInt();
        if(count < 0) return null;

        final byte[] bytes = new byte[count];
        buf.readBytes(bytes);

        return new String(bytes, CHARSET);
    }

    /**
     * Writes an integer array to a buffer.
     */
    public static void writeIntArray(ByteBuf buf, int[] array) {
        buf.writeInt(array.length);
        for (int value : array) {
            buf.writeInt(value);
        }
    }

    /**
     * Reads an integer array from a buffer.
     */
    public static int[] readIntArray(ByteBuf buf) {
        int length = buf.readInt();

        int[] array = new int[length];

        for (int i = 0; i < length; i++) {
            array[i] = buf.readInt();
        }

        return array;
    }

}
