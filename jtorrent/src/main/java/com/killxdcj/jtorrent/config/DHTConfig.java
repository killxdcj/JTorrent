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
    private int port = 6881;
    private List<String/* ip:port */> primeNodes = new ArrayList(){{
        add("router.bittorrent.com:6881");
        add("router.utorrent.com:6881");
        add("dht.transmissionbt.com:6881");
    }};
    private int nodesPerBuckets = 8;
    private int maxNodes = -1;
    private int findNodePeriod = 5 * 60 * 1000;
    private int nodeCheckPeriod = 10 * 60 * 1000;
    private int routingTableRebuildPerior = 30 * 60 * 1000;
    private int nodePingPeriod = 10 * 60 * 1000;

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

    public int getNodeCheckPeriod() {
        return nodeCheckPeriod;
    }

    public void setNodeCheckPeriod(int nodeCheckPeriod) {
        this.nodeCheckPeriod = nodeCheckPeriod;
    }

    public int getRoutingTableRebuildPerior() {
        return routingTableRebuildPerior;
    }

    public void setRoutingTableRebuildPerior(int routingTableRebuildPerior) {
        this.routingTableRebuildPerior = routingTableRebuildPerior;
    }

    public int getNodePingPeriod() {
        return nodePingPeriod;
    }

    public void setNodePingPeriod(int nodePingPeriod) {
        this.nodePingPeriod = nodePingPeriod;
    }
}
