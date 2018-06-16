package com.killxdcj.jtorrent.bencoding;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 14:45
 */
public class BencodedInteger extends AbstractBencodedValue {
    private long data;

    public BencodedInteger(String data) {
        this.data = Long.parseLong(data);
    }

    public BencodedInteger(long data) {
        this.data = data;
    }

    @Override
    public Long asLong() {
        return data;
    }

    public byte[] serialize() {
        byte[] integerBytes = String.valueOf(data).getBytes(DEFAULT_CHARSET);
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + integerBytes.length);
        byteBuffer.put(INTRGER_ENTRY);
        byteBuffer.put(integerBytes);
        byteBuffer.put(END_BYTE);
        return byteBuffer.array();
    }

    @Override
    public String toString() {
//        return "BencodedInteger{" +
//                "data=" + data +
//                "}";
        return "" + data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BencodedInteger that = (BencodedInteger) o;

        return data == that.data;
    }

    @Override
    public int hashCode() {
        return (int) (data ^ (data >>> 32));
    }

    @Override
    public Object toHuman() {
        return "" + data;
    }
}
