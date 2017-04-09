package com.killxdcj.jtorrent.utils;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.dht.Node;
import com.killxdcj.jtorrent.peer.Peer;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 11:51
 */
public class JTorrentUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JTorrentUtils.class);
    private static final Random RANDOM = new Random();

    public static BencodedString genNodeId() {
        StringBuilder sb = new StringBuilder();
        sb.append("com.killxdcj").append(new Date());
        return new BencodedString(DigestUtils.sha1(sb.toString()));
    }

    public static byte[] genByte(int length) {
        byte[] ret = new byte[length];
        RANDOM.nextBytes(ret);
        return ret;
    }

    public static byte[] compactNodeInfos(List<Node> nodes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(26 * nodes.size());
        for (Node node : nodes) {
            byteBuffer.put(node.getId().asBytes());
            byteBuffer.put(node.getAddr().getAddress());
            byteBuffer.put((byte) (0xff & (node.getPort() >> 8)));
            byteBuffer.put((byte) (0xff & node.getPort()));
            Integer xx = 100;
        }
        return byteBuffer.array();
    }

    public static byte[] compactNodeInfo(Node node) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(26);
        byteBuffer.put(node.getId().asBytes());
        byteBuffer.put(node.getAddr().getAddress());
        byteBuffer.put((byte) (0xff & (node.getPort() >> 8)));
        byteBuffer.put((byte) (0xff & node.getPort()));
        return byteBuffer.array();
    }

    public static List<Node> deCompactNodeInfos(byte[] compactData) {
        if (compactData.length % 26 != 0) {
            throw new UnsupportedOperationException("deCompactNodeInfos, data's length must be 26 * n bytes");
        }

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < compactData.length / 26; i++) {
            try {
                int startIdx = 26 * i;
                BencodedString noddId = new BencodedString(Arrays.copyOfRange(compactData, startIdx, startIdx + 20));
                InetAddress addr = InetAddress.getByAddress(Arrays.copyOfRange(compactData, startIdx + 20, startIdx + 24));
                int port = compactData[startIdx + 25] & 0xff | (0xff & compactData[startIdx + 24]) << 8;
                nodes.add(new Node(noddId, addr, port));
            } catch (Exception e) {
                LOGGER.error("DeCompactNodeInfo error, data:{}",
                        Arrays.toString(Arrays.copyOfRange(compactData, 26 * i, 26)), e);
            }
        }

        return nodes;
    }

    public static byte[] compactPeerInfo(Peer peer) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.put(peer.getAddr().getAddress());
        byteBuffer.put((byte) (0xff & (peer.getPort() >> 8)));
        byteBuffer.put((byte) (0xff & peer.getPort()));
        return byteBuffer.array();
    }

    public static Peer deCompactPeerInfo(byte[] data) {
        if (data.length != 6) {
            throw new UnsupportedOperationException("deCompactPeerInfo error, data's length must be 6 bytes");
        }

        try {
            InetAddress addr = InetAddress.getByAddress(Arrays.copyOfRange(data, 0, 4));
            int port = (0xff & data[4]) << 8 | data[5] & 0xff;
            return new Peer(addr, port);
        } catch (Exception e) {
            LOGGER.error("deCompactPeerInfo errot, data:{}", Arrays.toString(data), e);
            return null;
        }
    }

    private static final Map<Integer, Byte> bitMap = new HashMap() {{
        put(7, (byte) 1);
        put(6, (byte) 2);
        put(5, (byte) 4);
        put(4, (byte) 8);
        put(3, (byte) 16);
        put(2, (byte) 32);
        put(1, (byte) 64);
        put(0, (byte) 128);
    }};

    public static void setBit(byte[] data, int idx) {
        if (idx >= data.length * 8) {
            return;
        }

        int dataIndex = idx / 8;
        data[dataIndex] = (byte) (data[dataIndex] | bitMap.get(idx % 8));
    }

    public static long bytes2long(byte[] data) {
        if (data.length > 8) {
            throw new RuntimeException("leng must less then 8 byte");
        }

        long ret = 0;
        for (int idx = 0; idx < data.length; ++idx) {
            ret = ret << 8;
            ret = ret | (data[idx] & 0xff);
        }
        return ret;
    }
}
