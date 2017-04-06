package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.BencodedString;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 16:00
 */
public class RoutingTable {
    private Buckets[] bucketss = {new Buckets()};

    public RoutingTable() {
    }

    public RoutingTable(Buckets[] bucketss) {
        this.bucketss = bucketss;
    }

    synchronized public List<Node> findNode(BencodedString nodeId) {
        return bucketss[queryBuckets(nodeId)].getAllNode();
    }

    synchronized public List<Node> findNodeByInfoHash(BencodedString infoHash) {
        return bucketss[queryBuckets(infoHash)].getAllNode();
    }

    synchronized public void putNode(Node node) {
        int idx = queryBuckets(node.getId(), false);
        bucketss[idx].putNode(node);
        if (bucketss[idx].size() > Buckets.MAX_NODE_SIZE_PER_BUCKETS) {
            List<Buckets> divideBuckets = bucketss[idx].subdivide();
            Buckets[] newBuckets = new Buckets[bucketss.length + divideBuckets.size() - 1];
            System.arraycopy(bucketss, 0, newBuckets, 0, idx);
            for (int i = 0; i < divideBuckets.size(); i++) {
                newBuckets[idx + i] = divideBuckets.get(i);
            }
            System.arraycopy(bucketss, idx + 1, newBuckets, idx + divideBuckets.size(), bucketss.length - idx - 1);
            bucketss = newBuckets;
        }
    }

    synchronized public void removeNode(BencodedString nodeId) {
        bucketss[queryBuckets(nodeId, false)].removeNode(nodeId);
        // TODO when buckets's size is zero, merge buckets
    }

    synchronized public void removeNodes(List<BencodedString> nodeIds) {
        for (BencodedString nodeId : nodeIds) {
            bucketss[queryBuckets(nodeId, false)].removeNode(nodeId);
        }
        // TODO merge buckets if there are some buckets are empty
    }

    private int queryBuckets(BencodedString key) {
        return queryBuckets(key, true);
    }
    private int queryBuckets(BencodedString key, boolean ingoreEmpty) {
        int start = 0;
        int end = bucketss.length - 1;
        if (ingoreEmpty) {
            while (bucketss[start].size() == 0 && start < end) start++;
            while (bucketss[end].size() == 0 && end > 0) end--;
        }

        while (start < end) {
            int mid = (start + end) / 2;
            if (bucketss[mid].getId().compareTo(key) > 0) {
                end = mid - 1;
                if (ingoreEmpty) {
                    while (end > start && bucketss[end].size() == 0) end--;
                }
            } else if (bucketss[mid].getId().compareTo(key) == 0 && bucketss[mid].size() != 0) {
                return mid;
            } else {
                start = mid;
                if (ingoreEmpty) {
                    while (start < end && bucketss[start].size() == 0) start++;
                }
            }
        }

        return start;
    }

    // TODO every 1 hour rebuild routetable
}
