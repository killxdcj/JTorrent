package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.peer.Peer;
import com.killxdcj.jtorrent.utils.JTorrentUtils;
import com.killxdcj.jtorrent.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 16:00
 */
public class RoutingTable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingTable.class);

    private static final long LOG_STATS_PERIOD = 60 * 1000;
    private static final long REBUILD_PERIOD = 60 * 1000;

    private Buckets[] bucketss = {new Buckets()};
    private Set<BencodedString> nodeIds = new HashSet<>();
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5, r -> {
        Thread t = new Thread(r, "RoutingTable schedule");
        t.setDaemon(true);
        return t;
    });

    public RoutingTable() {
    }

    public RoutingTable(Buckets[] bucketss) {
        this.bucketss = bucketss;
    }

    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(this::logStats,0, LOG_STATS_PERIOD, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::rebuild, 0, REBUILD_PERIOD, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduledExecutorService.shutdown();
    }

    synchronized public List<Node> findNode(BencodedString nodeId) {
        return bucketss[queryBuckets(nodeId)].getAllNode();
    }

    synchronized public List<Node> findNodeByInfoHash(BencodedString infoHash) {
        return bucketss[queryBuckets(infoHash)].getAllNode();
    }

    synchronized public List<Node> pickNodeRandom() {
        return bucketss[JTorrentUtils.nextInt(bucketss.length)].getAllNode();
    }

    public List<Peer> findPeerByInfoHash(BencodedString infoHash) {
        // TODO
        return Collections.emptyList();
    }

    synchronized public void putNode(Node node) {
        int idx = queryBuckets(node.getId(), false);
        bucketss[idx].putNode(node);

        long oldSize = bucketss.length;
        nodeIds.add(node.getId());
        if (bucketss[idx].size() > Buckets.MAX_NODE_SIZE_PER_BUCKETS) {
            List<Buckets> divideBuckets = bucketss[idx].subdivide();
            if (divideBuckets.size() > 1) {
                Buckets[] newBuckets = new Buckets[bucketss.length + divideBuckets.size() - 1];
                System.arraycopy(bucketss, 0, newBuckets, 0, idx);
                for (int i = 0; i < divideBuckets.size(); i++) {
                    newBuckets[idx + i] = divideBuckets.get(i);
                }
                System.arraycopy(bucketss, idx + 1, newBuckets, idx + divideBuckets.size(), bucketss.length - idx - 1);
                bucketss = newBuckets;
            } else if (divideBuckets.size() == 1){
                bucketss[idx] = divideBuckets.get(0);
            } else {
                LOGGER.error("divede buckets error, size == 0, bucketsId:{}", bucketss[idx].getId().asHexString());
            }
            LOGGER.info("divede buckets, oldSize:{}, newSize:{}, divedeIdx：{}", oldSize, bucketss.length, idx);
        }
    }

    synchronized public void removeNode(BencodedString nodeId) {
        bucketss[queryBuckets(nodeId, false)].removeNode(nodeId);
        nodeIds.remove(nodeId);
        // TODO when buckets's size is zero, merge buckets
    }

    synchronized public void removeNodes(List<BencodedString> nodeIds) {
        for (BencodedString nodeId : nodeIds) {
            bucketss[queryBuckets(nodeId, false)].removeNode(nodeId);
        }
        // TODO merge buckets if there are some buckets are empty
    }

    public boolean contains(BencodedString nodeId) {
        return nodeIds.contains(nodeId);
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
            } else if (bucketss[mid].getId().compareTo(key) == 0) {
                if (ingoreEmpty) {
                    start = mid;
                    while (start < end && bucketss[start].size() == 0) start++;
                } else {
                    return mid;
                }
            } else {
                if (start == mid) {
                    if (bucketss[end].getId().compareTo(key) <= 0) {
                        return end;
                    } else {
                        return start;
                    }
                }

                start = mid;
                if (ingoreEmpty) {
                    while (start < end && bucketss[start].size() == 0) start++;
                }
            }
        }

        return start;
    }

    synchronized public void putPeer(BencodedString infoHash, Peer peer) {

    }

    synchronized public void putPeers(BencodedString infoHash, List<Peer> peers) {

    }

    synchronized public List<Node> getAllNode() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < bucketss.length; i++) {
            nodes.addAll(bucketss[i].getAllNode());
        }
        return nodes;
    }

    synchronized private void logStats() {
        LOGGER.info("RoutingTable logStats start");
        long startTime = TimeUtils.getCurTime();
        int total = 0;
        int notEmpty = 0;
        for (Buckets buckets : bucketss) {
            if (buckets.size() != 0) {
                notEmpty++;
                total += buckets.size();
            }
        }
        LOGGER.info("routing table stats, totalBckets:{}, notEmpty:{}, totalNodes:{}, costtime:{}ms",
                bucketss.length, notEmpty, total, TimeUtils.getElapseTime(startTime));
    }

    synchronized private void rebuild() {
        LOGGER.info("RoutingTable rebuild start");
        long startTime = TimeUtils.getCurTime();

        int total = 0;
        int notEmpty = 0;
        for (Buckets buckets : bucketss) {
            if (buckets.size() != 0) {
                notEmpty++;
                total += buckets.size();
            }
        }

        if (total > 100 && ((double) notEmpty / bucketss.length) < 0.8 ) {
            Buckets[] bucketssOld = bucketss;
            bucketss = new Buckets[1];
            bucketss[0] = new Buckets();

            for (Buckets buckets : bucketssOld) {
                buckets.getAllNode().forEach(node -> putNode(node));
            }
        }
        LOGGER.info("RoutingTable rebuild end, costtime:{}ms", TimeUtils.getElapseTime(startTime));
    }

    // TODO every 1 hour rebuild routetable
}
