package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.common.ConfigConsts;
import com.killxdcj.jtorrent.utils.TimeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 18:12
 */
public class Transaction {
    Node node;
    KRPC krpc;
    long expiredTime;

    public Transaction(Node node, KRPC krpc) {
        this.node = node;
        this.krpc = krpc;
        expiredTime = TimeUtils.getExpiredTime(ConfigConsts.TRANSACTION_VALID_PERIOD);
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

    public boolean isExpired() {
        if (TimeUtils.getCurTime() > expiredTime) return true;
        return false;
    }
}