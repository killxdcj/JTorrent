package com.killxdcj.jtorrent.example;

import com.alibaba.fastjson.JSON;
import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.bencoding.Bencoding;
import com.killxdcj.jtorrent.config.DHTConfig;
import com.killxdcj.jtorrent.dht.DHT;
import com.killxdcj.jtorrent.dht.IDHTCallBack;
import com.killxdcj.jtorrent.peer.MetadataFetcher;
import com.killxdcj.jtorrent.peer.Peer;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/12
 * Time: 00:08
 */
public class MetadataFetcherExample {

    public static String timex() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fmt.format(new Date()) + " ";
    }

    public static void main(String[] args) {
        DHT dht = new DHT(new DHTConfig());
        Set<BencodedString> fetchedHash = new HashSet<>();
        IDHTCallBack callBack = new IDHTCallBack() {
            @Override
            public void onGetPeers(BencodedString infohash, List<Peer> peers) {
                for (Peer peer : peers) {
                    try {
                        MetadataFetcher fetcher = new MetadataFetcher(peer, infohash, new MetadataFetcher.IFetcherCallback() {
                            @Override
                            public void onFinshed(BencodedString infohash, byte[] metadata) {
//                                if (!fetchedHash.contains(infohash)) {
                                    Bencoding bencoding = new Bencoding(metadata);
                                    try {
                                        System.out.println(MetadataFetcherExample.timex() + "meta geted, " + infohash.asHexString() + ":" + JSON.toJSONString(bencoding.decode().toHuman()));
                                    } catch (Exception e) {
                                        System.out.println(MetadataFetcherExample.timex() + "metadata fetch ok, but parse failed" + infohash.asHexString() + ",peer:" + peer);
                                        e.printStackTrace();
                                    }
//                                    dht.markPeerGood(infohash, peer);
//                                }

//                                fetchedHash.add(infohash);
                            }

                            @Override
                            public void onTimeout() {
                                System.out.println(MetadataFetcherExample.timex() + "metadata fetch timeout, " + infohash.asHexString() + ",peer:" + peer);
//                                dht.markPeerBad(infohash, peer);
                            }

                            @Override
                            public void onException(Exception e) {
                                System.out.println(MetadataFetcherExample.timex() + "metadata fetch exceprion, " + infohash.asHexString() + ",peer:" + peer);
//                                dht.markPeerBad(infohash, peer);
//                                e.printStackTrace();
                            }
                        });
                        new Thread(fetcher).start();
                        System.out.println(MetadataFetcherExample.timex() + "metadata fetch start, " + infohash.asHexString() + ",peer:" + peer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onGetInfoHash(BencodedString infohash) {
                System.out.println(MetadataFetcherExample.timex() + "catch infohash:" + infohash.asHexString());
//                dht.queryPeers(infohash, this);
            }

            @Override
            public void onAnnouncePeer(BencodedString infohash, Peer peer, String tag) {
//                System.out.println("catch announce peer:" + infohash.asHexString() + ", peer:" + peer);
                List<Peer> peers = new ArrayList<>();
                peers.add(peer);
                onGetPeers(infohash, peers);
//                if (!fetchedHash.contains(infohash)) {
//                    List<Peer> peers = new ArrayList<>();
//                    peers.add(peer);
//                    onGetPeers(infohash, peers);
//                }
            }
        };

        try {
            dht.setCallBack(callBack);
            dht.start();
//            dht.queryPeers("E420AEC08DD5EDEDBA92CD79ABC8EEFA11CFAB29");
//            dht.queryPeers("08b07db51ab63e93575fa5f37ef32b36731d35e8");
//            dht.queryPeers("ffbe21a1ab5973e399eada5cc5be0a7dc33c9635");
//            dht.queryPeers("bcf2c70e4d37974fb98079c717ff1df50b484ce7");
//            dht.queryPeers("999A130863F3D17C48C35D0364456EB2A429EDA1");
//            dht.queryPeers("33D080C5AF48D264072CC2FA4CF9DBE0EE84EB2C");
            while (true) {
                Thread.sleep(30 * 60 * 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dht.shutdown();
    }
}
