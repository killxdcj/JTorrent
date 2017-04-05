package com.killxdcj.jtorrent.peer;

import java.net.InetAddress;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 17:29
 */
public class Peer {
    private InetAddress addr;
    private int port;

    public Peer(InetAddress addr, int port) {
        this.addr = addr;
        this.port = port;
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

    @Override
    public String toString() {
        return "Peer{" +
                "addr=" + addr +
                ", port=" + port +
                '}';
    }
}
