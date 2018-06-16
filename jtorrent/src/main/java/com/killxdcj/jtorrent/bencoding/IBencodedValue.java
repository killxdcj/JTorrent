package com.killxdcj.jtorrent.bencoding;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 14:38
 */
public interface IBencodedValue {
    String asString();
    byte[] asBytes();
    Long asLong();
    List<IBencodedValue> asList();
    Map<String, IBencodedValue> asMap();
    byte[] serialize();
    Object toHuman();
}
