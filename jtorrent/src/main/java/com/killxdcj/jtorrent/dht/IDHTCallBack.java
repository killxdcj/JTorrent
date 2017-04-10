package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.peer.Peer;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 17:34
 */
public interface IDHTCallBack {
    public void onGetPeers(BencodedString infohash, List<Peer> peers);
    public void onGetInfoHash(BencodedString infohash);
    public void onAnnouncePeer(BencodedString infohash, Peer peer);
}
