package com.killxdcj.jtorrent.bencoding;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 14:32
 */
public class Bencoding {
    private byte[] data;
    private int curIndex;
    private int maxLength;

    public Bencoding(byte[] data) {
        this.data = data;
    }

    public Bencoding(byte[] data, int offset, int length) {
        this.data = Arrays.copyOfRange(data, offset, length);
    }

    public IBencodedValue decode() throws Exception {
        this.data = data;
        maxLength = data.length;
        return readObject();
    }

    private BencodedMap readDictionarie() throws Exception {
        BencodedMap ret = new BencodedMap();
        curIndex++;
        while (curIndex < maxLength && data[curIndex] != AbstractBencodedValue.END_BYTE) {
            String key = readString().asString();
            IBencodedValue value = readObject();
            ret.put(key, value);
        }
        curIndex++;
        return ret;
    }

    private BencodedList readList() throws Exception {
        BencodedList ret = new BencodedList();
        curIndex = curIndex + 1;
        while (curIndex < maxLength && data[curIndex] != AbstractBencodedValue.END_BYTE) {
            ret.add(readObject());
        }
        curIndex++;
        return ret;
    }

    private BencodedString readString() {
        int index = indexOf(AbstractBencodedValue.STRING_SPLIT, curIndex);
        int length = Integer.parseInt(new String(data, curIndex, index - curIndex));
        curIndex = index + 1 + length;
        return new BencodedString(data, index + 1, length);
    }

    private BencodedInteger readInteger() {
        int index = indexOf(AbstractBencodedValue.END_BYTE, curIndex);
        long ret = Long.parseLong(new String(data, curIndex + 1, index - curIndex - 1));
        curIndex = index + 1;
        return new BencodedInteger(ret);
    }

    private IBencodedValue readObject() throws Exception {
        if (data[curIndex] == AbstractBencodedValue.MAP_ENTRY) {
            return readDictionarie();
        } else if (data[curIndex] == AbstractBencodedValue.LIST_ENTRY) {
            return readList();
        } else if (data[curIndex] == AbstractBencodedValue.INTRGER_ENTRY) {
            return readInteger();
        } else if (AbstractBencodedValue.STRING_ENTRYS.contains(data[curIndex])) {
            return readString();
        } else {
            throw new Exception("unknow data type, index:" + curIndex + ",type:" + data[curIndex]);
        }
    }

    private int indexOf(byte tar, int index) {
        while (index++ < maxLength) {
            if (data[index] == tar) {
                return index;
            }
        }

        return -1;
    }
}
