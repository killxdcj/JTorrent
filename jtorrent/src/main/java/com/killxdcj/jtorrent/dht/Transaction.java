package com.killxdcj.jtorrent.dht;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 18:12
 */
public class Transaction {
    Node node;
    KRPC krpc;

    public Transaction(Node node, KRPC krpc) {
        this.node = node;
        this.krpc = krpc;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public KRPC getKrpc() {
        return krpc;
    }

    public void setKrpc(KRPC krpc) {
        this.krpc = krpc;
    }
}