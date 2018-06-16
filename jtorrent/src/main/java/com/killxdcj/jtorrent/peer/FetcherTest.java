package com.killxdcj.jtorrent.peer;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import org.apache.commons.codec.binary.Hex;

import java.net.InetAddress;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/10
 * Time: 22:28
 */
public class FetcherTest {
    public static void main(String[] args) {
        try {
            MetadataFetcher fetcher = new MetadataFetcher(InetAddress.getByName("111.167.156.226"), 8208, new BencodedString(Hex.decodeHex("1e92b495401898b91819bb19325438df51410aba".toCharArray())), new MetadataFetcher.IFetcherCallback() {
                @Override
                public void onFinshed(BencodedString infohash, byte[] metadata) {
                    System.out.println("ok");
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
