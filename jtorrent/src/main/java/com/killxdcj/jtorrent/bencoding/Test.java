package com.killxdcj.jtorrent.bencoding;

import com.killxdcj.jtorrent.config.DHTConfig;
import com.killxdcj.jtorrent.dht.DHT;
import com.killxdcj.jtorrent.utils.JTorrentUtils;
import org.apache.commons.codec.binary.Hex;

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
            dht.queryPeers("E420AEC08DD5EDEDBA92CD79ABC8EEFA11CFAB29");
            dht.queryPeers("08b07db51ab63e93575fa5f37ef32b36731d35e8");
            dht.queryPeers("ffbe21a1ab5973e399eada5cc5be0a7dc33c9635");
            while (true) {
                Thread.sleep(30 * 60 * 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dht.shutdown();
//        char[] xx = new char[40];
//        for (int i = 0; i < xx.length; i++) {
//            xx[i] = '0';
//        }
//        xx[0] = '8';
//
//        try {
////            System.out.println(JTorrentUtils.bytes2long(Hex.decodeHex(xx)));
//            BencodedString yy = new BencodedString(Hex.decodeHex(xx));
//            xx[0] = '9';
//            BencodedString zz = new BencodedString(Hex.decodeHex(xx));
//            System.out.println(yy.compareTo(zz));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
