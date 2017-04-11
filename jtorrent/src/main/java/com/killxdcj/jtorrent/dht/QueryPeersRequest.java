package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.utils.TimeUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/07
 * Time: 20:43
 */
public class QueryPeersRequest {
    private BencodedString infohash;
    private long startTime;
    private IDHTCallBack idhtCallBack;
    private Set<BencodedString> ignoreNode = new HashSet<>();
    private boolean continueQuery = true;
    private volatile long queryTimes = 0;

    public QueryPeersRequest(BencodedString infohash) {
        this.infohash = infohash;
        startTime = TimeUtils.getCurTime();
    }

    public QueryPeersRequest(BencodedString infohash, IDHTCallBack idhtCallBack) {
        this.infohash = infohash;
        this.idhtCallBack = idhtCallBack;
        startTime = TimeUtils.getCurTime();
    }

    public BencodedString getInfohash() {
        return infohash;
    }

    public void setInfohash(BencodedString infohash) {
        this.infohash = infohash;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public IDHTCallBack getIdhtCallBack() {
        return idhtCallBack;
    }

    public void setIdhtCallBack(IDHTCallBack idhtCallBack) {
        this.idhtCallBack = idhtCallBack;
    }

    synchronized public void ignore(BencodedString nodeId) {
        ignoreNode.add(nodeId);
    }

    public boolean isIgnore(BencodedString nodeId) {
        return ignoreNode.contains(nodeId);
    }

    public long ignoreSize() {
        return ignoreNode.size();
    }

    public boolean isContinueQuery() {
        return continueQuery;
    }

    public void setContinueQuery(boolean continueQuery) {
        this.continueQuery = continueQuery;
    }

    public void markStopQuery() {
        continueQuery = false;
    }

    public long aliveTime() {
        return TimeUtils.getElapseTime(startTime, TimeUnit.MILLISECONDS);
    }
}
