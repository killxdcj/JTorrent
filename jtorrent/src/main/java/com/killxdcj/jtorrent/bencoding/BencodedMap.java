package com.killxdcj.jtorrent.bencoding;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 14:44
 */
public class BencodedMap extends AbstractBencodedValue {
    private Map<String, IBencodedValue> data;

    public BencodedMap() {
        data = new HashMap();
    }

    public BencodedMap(Map<String, IBencodedValue> data) {
        this.data = data;
    }

    public void put(String key, IBencodedValue value) {
        data.put(key, value);
    }

    public IBencodedValue get(String key) {
        return data.get(key);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    @Override
    public Map<String, IBencodedValue> asMap() {
        return data;
    }

    public byte[] serialize() {
        int totalLength = 2;
        Map<byte[], byte[]> dataBytes = new HashMap();
        for (Map.Entry<String, IBencodedValue> entry : data.entrySet()) {
            byte[] keyBytes = new BencodedString(entry.getKey()).serialize();
            byte[] valueBytes = entry.getValue().serialize();
            dataBytes.put(keyBytes, valueBytes);
            totalLength += keyBytes.length;
            totalLength += valueBytes.length;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength);
        byteBuffer.put(MAP_ENTRY);
        for (Map.Entry<byte[], byte[]> entry : dataBytes.entrySet()) {
            byteBuffer.put(entry.getKey());
            byteBuffer.put(entry.getValue());
        }
        byteBuffer.put(END_BYTE);
        return byteBuffer.array();
    }

    @Override
    public String toString() {
        return "{" + data + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BencodedMap that = (BencodedMap) o;

        return data != null ? data.equals(that.data) : that.data == null;
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public Object toHuman() {
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, IBencodedValue> entry : data.entrySet()) {
            if (entry.getKey().equals("pieces")) {
//                ret.put(entry.getKey(), ((BencodedString)entry.getValue()).asHexString());
                ret.put(entry.getKey(), "pieces and ignore");
            } else {
                System.out.println("not pieces :" + entry.getKey());
                ret.put(entry.getKey(), entry.getValue().toHuman());
            }
        }
        return ret;
    }
}
