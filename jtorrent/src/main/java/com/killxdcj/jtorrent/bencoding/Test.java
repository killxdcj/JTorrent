package com.killxdcj.jtorrent.bencoding;

import com.killxdcj.jtorrent.config.DHTConfig;
import com.killxdcj.jtorrent.dht.DHT;
import com.killxdcj.jtorrent.dht.IDHTCallBack;
import com.killxdcj.jtorrent.dht.Node;
import com.killxdcj.jtorrent.peer.MetadataFetcher;
import com.killxdcj.jtorrent.peer.Peer;
import com.killxdcj.jtorrent.utils.JTorrentUtils;
import org.apache.commons.codec.binary.Hex;

import java.net.InetAddress;
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
//        try {
//            System.out.println(new String(Hex.decodeHex("372e31302e32312e323330".toCharArray())));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        DHT dht = new DHT(new DHTConfig());
        IDHTCallBack callBack = new IDHTCallBack() {
            @Override
            public void onGetPeers(BencodedString infohash, List<Peer> peers) {
                for (Peer peer : peers) {
                    try {
                        MetadataFetcher fetcher = new MetadataFetcher(peer, infohash, new MetadataFetcher.IFetcherCallback() {
                            @Override
                            public void onFinshed(BencodedString infohash, byte[] metadata) {
                                System.out.println("fetch ok, " + infohash.asHexString() + ",peer:" + peer);
                                Bencoding bencoding = new Bencoding(metadata);
                                System.out.println(bencoding.toString());
                                dht.markPeerGood(infohash, peer);
                            }

                            @Override
                            public void onTimeout() {
                                System.out.println("fetch timeout, " + infohash.asHexString() + ",peer:" + peer);
                            }

                            @Override
                            public void onException(Exception e) {
                                System.out.println("fetch exceprion, " + infohash.asHexString() + ",peer:" + peer);
                                e.printStackTrace();
                            }
                        });
                        new Thread(fetcher).start();
                        System.out.println("fetch start, " + infohash.asHexString() + ",peer:" + peer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onGetInfoHash(BencodedString infohash) {
                System.out.println("get infohash:" + infohash.asHexString());
                dht.queryPeers(infohash, this);
            }

            @Override
            public void onAnnouncePeer(BencodedString infohash, Peer peer) {
                System.out.println("get announce peer:" + infohash.asHexString() + ", peer:" + peer);
                List<Peer> peers = new ArrayList<>();
                peers.add(peer);
                onGetPeers(infohash, peers);
            }
        };

        try {
            dht.setCallBack(callBack);
            dht.start();
            dht.queryPeers("E420AEC08DD5EDEDBA92CD79ABC8EEFA11CFAB29");
            dht.queryPeers("08b07db51ab63e93575fa5f37ef32b36731d35e8");
            dht.queryPeers("ffbe21a1ab5973e399eada5cc5be0a7dc33c9635");
            dht.queryPeers("bcf2c70e4d37974fb98079c717ff1df50b484ce7");
            dht.queryPeers("999A130863F3D17C48C35D0364456EB2A429EDA1");
            dht.queryPeers("33D080C5AF48D264072CC2FA4CF9DBE0EE84EB2C");
            while (true) {
                Thread.sleep(30 * 60 * 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dht.shutdown();
    }
}
