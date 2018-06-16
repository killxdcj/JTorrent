package com.killxdcj.jtorrent.peer;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.bencoding.Bencoding;
import com.killxdcj.jtorrent.bencoding.IBencodedValue;
import org.apache.commons.codec.binary.Hex;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/10
 * Time: 22:28
 */
public class FetcherTest {
    public static void main(String[] args) {
        try {
            MetadataFetcher fetcher = new MetadataFetcher(InetAddress.getByName("112.99.69.48"), 34744, new BencodedString(Hex.decodeHex("ff325954c779a50b9db92a3134da40029d4589e9".toCharArray())), new MetadataFetcher.IFetcherCallback() {
                @Override
                public void onFinshed(BencodedString infohash, byte[] metadata) {
                    System.out.println("ok");
                    String xx = "";
                    for (byte x : metadata) {
                        xx += (char)x;
                    }
                    System.out.println(xx);
                    try {
                        IBencodedValue x = (new Bencoding(metadata)).decode();
                        System.out.println(x.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onTimeout() {
                    System.out.println("timeout");
                }

                @Override
                public void onException(Exception e) {
                    e.printStackTrace();
                }
            });
            Thread thread = new Thread(fetcher);
            thread.start();
            while (true) {
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
