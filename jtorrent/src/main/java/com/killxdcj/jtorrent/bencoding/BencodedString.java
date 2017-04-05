package com.killxdcj.jtorrent.bencoding;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 14:44
 */
public class BencodedString extends AbstractBencodedValue {
    private byte[] data;

    public BencodedString(byte[] data) {
        this.data = data;
    }

    public BencodedString(String str) {
        data = str.getBytes(DEFAULT_CHARSET);
    }

    public BencodedString(byte bytes[], int offset, int length) {
        data = Arrays.copyOfRange(bytes, offset, offset + length);
    }

    @Override
    public byte[] asBytes() {
        return data;
    }

    @Override
    public String asString() {
        return new String(data, DEFAULT_CHARSET);
    }

    public byte[] serialize() {
        byte[] lengthBytes = String.valueOf(data.length).getBytes(DEFAULT_CHARSET);
        ByteBuffer byteBuffer = ByteBuffer.allocate(lengthBytes.length + 1 + data.length);
        byteBuffer.put(lengthBytes);
        byteBuffer.put(STRING_SPLIT);
        byteBuffer.put(data);
        return byteBuffer.array();
    }

    @Override
    public String toString() {
        return "BencodedString{" +
                "data=" + new String(data, DEFAULT_CHARSET) +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BencodedString that = (BencodedString) o;

        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
