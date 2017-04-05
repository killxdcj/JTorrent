package com.killxdcj.jtorrent.bencoding;

import com.killxdcj.jtorrent.config.DHTConfig;
import com.killxdcj.jtorrent.dht.DHT;
import com.killxdcj.jtorrent.utils.JTorrentUtils;

import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/03
 * Time: 23:23
 */
public class Test {
    public static void main(String[] args) {
//        BencodedMap map = new BencodedMap();
//        map.put("str", new BencodedString("caojianhua"));
//        BencodedList list = new BencodedList();
//        list.add(new BencodedString("曹建华"));
//        list.add(new BencodedInteger(1000));
//        map.put("list", list);
//        map.put("int", new BencodedInteger(1000));
//        for (byte bt : map.serialize()) {
//            System.out.print((char) bt);
//        }
//
//        try {
//            Bencoding bencoding = new Bencoding(map.serialize());
//            System.out.println("\r\n" + bencoding.decode());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        DHT dht = new DHT(new DHTConfig());
        try {
            dht.start();
            Thread.sleep(5000);
            dht.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
