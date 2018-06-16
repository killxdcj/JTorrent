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
            MetadataFetcher fetcher = new MetadataFetcher(InetAddress.getByName("175.149.220.141"), 7891, new BencodedString(Hex.decodeHex("c83d68b386af249a4412bc4f68ec2efb2149398e".toCharArray())), new MetadataFetcher.IFetcherCallback() {
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
