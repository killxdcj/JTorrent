package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.utils.JTorrentUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 16:00
 */
public class Buckets {
    public static final int MAX_NODE_SIZE_PER_BUCKETS = 8;

    private BencodedString id;
    private int idPrefixLength;

    private Map<BencodedString, Node> nodeTable = new HashMap<>();

    public Buckets() {
        byte[] idByte = new byte[20];
        for (int i = 0; i < idByte.length; i++) {
            idByte[0] = 0;
        }

        id = new BencodedString(idByte);
        idPrefixLength = 0;
    }

    public Buckets(BencodedString id, int idPrefixLength) {
        this.id = id;
        this.idPrefixLength = idPrefixLength;
    }

    public BencodedString getId() {
        return id;
    }

    public void putNode(Node node) {
        nodeTable.put(node.getId(), node);
    }

    public Node getNode(BencodedString nodeId) {
        return nodeTable.get(nodeId);
    }

    public void removeNode(BencodedString nodeId) {
        nodeTable.remove(nodeId);
    }

    public List<Node> getAllNode() {
        return nodeTable.values().stream().collect(Collectors.toList());
    }

    public int size() {
        return nodeTable.size();
    }

    public List<Buckets> subdivide() {
        if (idPrefixLength == 8 * id.asBytes().length - 1) {
            // TODO remove the oldest node
            return null;
        }

        List<Buckets> rets = new ArrayList<>();
        byte[] smallId = Arrays.copyOf(id.asBytes(), id.asBytes().length);
        byte[] largeId = Arrays.copyOf(id.asBytes(), id.asBytes().length);
        JTorrentUtils.setBit(largeId, idPrefixLength + 1);
        Buckets smallBuckets = new Buckets(new BencodedString(smallId), idPrefixLength + 1);
        Buckets largeBuckets = new Buckets(new BencodedString(largeId), idPrefixLength + 1);
        nodeTable.forEach((k, v) -> {
            if (k.compareTo(largeBuckets.getId()) >= 0) {
                largeBuckets.putNode(v);
            } else {
                smallBuckets.putNode(v);
            }
        });

        if (smallBuckets.size() > MAX_NODE_SIZE_PER_BUCKETS) {
            rets.addAll(reduceBuckets(smallBuckets.subdivide()));
        } else {
            rets.add(smallBuckets);
        }

        if (largeBuckets.size() > MAX_NODE_SIZE_PER_BUCKETS) {
            rets.addAll(reduceBuckets(largeBuckets.subdivide()));
        } else {
            rets.add(largeBuckets);
        }

        return rets;
    }

    private List<Buckets> reduceBuckets(List<Buckets> bucketss) {
        List<Buckets> rets = new ArrayList<>();
        Buckets smallest = null;
        for (Buckets buckets : bucketss) {
            if (buckets.size() > 0) {
                rets.add(buckets);
            } else if (smallest == null) {
                smallest = buckets;
            } else if (buckets.getId().compareTo(smallest) < 0) {
                smallest = buckets;
            }
        }
        rets.add(smallest);

        return rets;
    }
}
