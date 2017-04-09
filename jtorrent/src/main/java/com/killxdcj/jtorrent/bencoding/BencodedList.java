package com.killxdcj.jtorrent.bencoding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 14:45
 */
public class BencodedList extends AbstractBencodedValue {
    private List<IBencodedValue> datas;

    public BencodedList() {
        datas = new ArrayList();
    }

    public BencodedList(List<IBencodedValue> datas) {
        this.datas = datas;
    }

    @Override
    public List<IBencodedValue> asList() {
        return datas;
    }

    public void add(IBencodedValue value) {
        datas.add(value);
    }

    public int size() {
        return datas.size();
    }

    public IBencodedValue get(int index) {
        return datas.get(index);
    }

    public byte[] serialize() {
        int totalLength = 2;
        List<byte[]> elements = new ArrayList();
        for (IBencodedValue element : datas) {
            byte[] bytes = element.serialize();
            totalLength += bytes.length;
            elements.add(bytes);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength);
        byteBuffer.put(LIST_ENTRY);
        for (byte[] element : elements) {
            byteBuffer.put(element);
        }
        byteBuffer.put(END_BYTE);
        return byteBuffer.array();
    }

    @Override
    public String toString() {
        return "" + datas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BencodedList that = (BencodedList) o;

        return datas != null ? datas.equals(that.datas) : that.datas == null;
    }

    @Override
    public int hashCode() {
        return datas != null ? datas.hashCode() : 0;
    }
}
