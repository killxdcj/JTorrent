package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.utils.TimeUtils;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 15:58
 */
public class Node {
    BencodedString id;
    private InetAddress addr;
    int port;
    long lastActive;

    public Node(InetAddress addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public Node(BencodedString id, InetAddress addr, int port) {
        this.id = id;
        this.addr = addr;
        this.port = port;
        this.lastActive = TimeUtils.getCurTime();
    }

    public BencodedString getId() {
        return id;
    }

    public void setId(BencodedString id) {
        this.id = id;
    }

    public InetAddress getAddr() {
        return addr;
    }

    public void setAddr(InetAddress addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", addr=" + addr +
                ", port=" + port +
                ", lastActive=" + lastActive +
                '}';
    }
}
