package com.killxdcj.jtorrent.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 16:03
 */
public class DHTConfig {
    private int message_max_size = 10 * 1024 * 1024;
    private int port = 8888;
    private List<String/* ip:port */> primeNodes = new ArrayList(){{
        add("router.bittorrent.com:6881");
        add("router.utorrent.com:6881");
        add("dht.transmissionbt.com:6881");
    }};
    private int nodesPerBuckets = 8;
    private int maxNodes = -1;
    private int findNodePeriod =  1000;
    private int routingTableRebuildPerior = 30 * 60 * 1000;
    private int nodePingCheckPeriod = 60 * 1000;
    private int nodeMaxUnactiveTimeBeforePing = 10 * 60 * 1000;
    private int nodeMaxUnactiveTime = 30 * 60 * 1000;
    private int queryPeersRequestCheckPeriod = 1 * 60 * 1000;
    private int queryPeersRequestMaxAliveTime = 24 * 60 * 60 * 1000;
    private int queryPeersRequestTimeout = 23 * 60 * 60 * 1000;
    private int queryPeersRequestMaxqueryTimes = 20000;

    public int getMessage_max_size() {
        return message_max_size;
    }

    public void setMessage_max_size(int message_max_size) {
        this.message_max_size = message_max_size;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<String> getPrimeNodes() {
        return primeNodes;
    }

    public void setPrimeNodes(List<String> primeNodes) {
        this.primeNodes = primeNodes;
    }

    public int getNodesPerBuckets() {
        return nodesPerBuckets;
    }

    public void setNodesPerBuckets(int nodesPerBuckets) {
        this.nodesPerBuckets = nodesPerBuckets;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public int getFindNodePeriod() {
        return findNodePeriod;
    }

    public void setFindNodePeriod(int findNodePeriod) {
        this.findNodePeriod = findNodePeriod;
    }

    public int getRoutingTableRebuildPerior() {
        return routingTableRebuildPerior;
    }

    public void setRoutingTableRebuildPerior(int routingTableRebuildPerior) {
        this.routingTableRebuildPerior = routingTableRebuildPerior;
    }

    public int getNodePingCheckPeriod() {
        return nodePingCheckPeriod;
    }

    public void setNodePingCheckPeriod(int nodePingCheckPeriod) {
        this.nodePingCheckPeriod = nodePingCheckPeriod;
    }

    public int getNodeMaxUnactiveTimeBeforePing() {
        return nodeMaxUnactiveTimeBeforePing;
    }

    public void setNodeMaxUnactiveTimeBeforePing(int nodeMaxUnactiveTimeBeforePing) {
        this.nodeMaxUnactiveTimeBeforePing = nodeMaxUnactiveTimeBeforePing;
    }

    public int getNodeMaxUnactiveTime() {
        return nodeMaxUnactiveTime;
    }

    public void setNodeMaxUnactiveTime(int nodeMaxUnactiveTime) {
        this.nodeMaxUnactiveTime = nodeMaxUnactiveTime;
    }

    public int getQueryPeersRequestCheckPeriod() {
        return queryPeersRequestCheckPeriod;
    }

    public void setQueryPeersRequestCheckPeriod(int queryPeersRequestCheckPeriod) {
        this.queryPeersRequestCheckPeriod = queryPeersRequestCheckPeriod;
    }

    public int getQueryPeersRequestMaxAliveTime() {
        return queryPeersRequestMaxAliveTime;
    }

    public void setQueryPeersRequestMaxAliveTime(int queryPeersRequestMaxAliveTime) {
        this.queryPeersRequestMaxAliveTime = queryPeersRequestMaxAliveTime;
    }

    public int getQueryPeersRequestTimeout() {
        return queryPeersRequestTimeout;
    }

    public void setQueryPeersRequestTimeout(int queryPeersRequestTimeout) {
        this.queryPeersRequestTimeout = queryPeersRequestTimeout;
    }

    public int getQueryPeersRequestMaxqueryTimes() {
        return queryPeersRequestMaxqueryTimes;
    }

    public void setQueryPeersRequestMaxqueryTimes(int queryPeersRequestMaxqueryTimes) {
        this.queryPeersRequestMaxqueryTimes = queryPeersRequestMaxqueryTimes;
    }
}
