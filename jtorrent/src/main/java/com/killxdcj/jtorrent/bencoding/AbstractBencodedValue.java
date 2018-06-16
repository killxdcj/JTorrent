package com.killxdcj.jtorrent.bencoding;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 14:41
 */
public abstract class AbstractBencodedValue implements IBencodedValue {
    public static final byte STRING_SPLIT = (byte) ':';
    public static final byte INTRGER_ENTRY = (byte) 'i';
    public static final byte MAP_ENTRY = (byte) 'd';
    public static final byte LIST_ENTRY = (byte) 'l';
    public static final byte END_BYTE = (byte) 'e';
    public static final Set<Byte> STRING_ENTRYS = new HashSet<Byte>() {{
        add((byte) '1');
        add((byte) '2');
        add((byte) '3');
        add((byte) '4');
        add((byte) '5');
        add((byte) '6');
        add((byte) '7');
        add((byte) '8');
        add((byte) '9');
    }};
    protected static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public String asString() {
        throw new UnsupportedOperationException("Bencoded value cannot be converted to string");
    }

    public byte[] asBytes() {
        throw new UnsupportedOperationException("Bencoded value cannot be converted to bytes");
    }

    public Long asLong() {
        throw new UnsupportedOperationException("Bencoded value cannot be converted to long");
    }

    public List<IBencodedValue> asList() {
        throw new UnsupportedOperationException("Bencoded value cannot be converted to list");
    }

    public Map<String, IBencodedValue> asMap() {
        throw new UnsupportedOperationException("Bencoded value cannot be converted to map");
    }

    @Override
    public Object toHuman() {
        throw new UnsupportedOperationException("Bencoded value cannot be converted to map");
    }
}
