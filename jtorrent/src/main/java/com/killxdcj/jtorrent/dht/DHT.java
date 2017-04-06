package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.*;
import com.killxdcj.jtorrent.config.DHTConfig;
import com.killxdcj.jtorrent.peer.Peer;
import com.killxdcj.jtorrent.utils.JTorrentUtils;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 15:57
 */
public class DHT {
    private static final Logger LOGGER = LoggerFactory.getLogger(DHT.class);

    private DHTConfig config;

    private volatile boolean shutdown = false;
    private BencodedString nodeId;
    private DatagramSocket datagramSocket;
    private TransactionManager transactionManager;
    private ExecutorService worker = Executors.newSingleThreadExecutor();

    public DHT(DHTConfig config) {
        this.config = config;
        nodeId = JTorrentUtils.genNodeId();
        transactionManager = new TransactionManager();
    }

    public void start() throws SocketException {
        datagramSocket = new DatagramSocket(config.getPort());
        worker.submit(this::workProc);
        try {
            BencodedString infohash = new BencodedString(Hex.decodeHex("546cf15f724d19c4319cc17b179d7e035f89c1f4".toCharArray()));
            Node node = null;
//            node = new Node(InetAddress.getByName("router.bittorrent.com"), 6881);
//            sendPingReq(node);
//            sendGetPeerReq(node, infohash);
            node = new Node(InetAddress.getByName("router.utorrent.com"), 6881);
            sendPingReq(node);
            sendGetPeerReq(node, infohash);
//            node = new Node(InetAddress.getByName("dht.transmissionbt.com"), 6881);
//            sendPingReq(node);
//            sendGetPeerReq(node, infohash);

//            node = new Node(InetAddress.getByName("5.189.183.129"), 46907);
//            sendPingReq(node);
//            sendGetPeerReq(node, infohash);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        shutdown = true;
        worker.shutdown();
        datagramSocket.close();
    }

    public List<Peer> queryPeers(byte[] infohash) {
        return null;
    }

    public List<Peer> queryPeers(byte[] infohash, IDHTCallBack callBack) {
        return null;
    }

    private void sendPingReq(Node node) {
        try {
            KRPC krpc = KRPC.buildPingReqPacket(this.nodeId);
            sendKrpcPacket(node, krpc);
            transactionManager.putTransaction(new Transaction(node, krpc));
        } catch (Exception e) {
            LOGGER.error("sendPingReq error, node:{}", node, e);
        }
    }

    private void sendFindNodeReq(Node node, BencodedString targetNodeId) {
        try {
            KRPC krpc = KRPC.buildFindNodeReqPacket(this.nodeId, targetNodeId);
            sendKrpcPacket(node, krpc);
            transactionManager.putTransaction(new Transaction(node, krpc));
        } catch (Exception e) {
            LOGGER.error("sendFindNodeReq error, node:{}", node, e);
        }
    }

    private void sendGetPeerReq(Node node, BencodedString infohash) {
        try {
            KRPC krpc = KRPC.buildGetPeersReqPacket(this.nodeId, infohash);
            sendKrpcPacket(node, krpc);
            transactionManager.putTransaction(new Transaction(node, krpc));
        } catch (Exception e) {
            LOGGER.error("sendGetPeerReq error, node:{}", node, e);
        }
    }

    private void sendKrpcPacket(Node node, KRPC krpc) throws IOException {
        byte[] packetBytes = krpc.encode();
        DatagramPacket udpPacket = new DatagramPacket(packetBytes, 0, packetBytes.length, node.getAddr(), node.getPort());
        datagramSocket.send(udpPacket);
    }

    private void workProc() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[config.getMessage_max_size()], config.getMessage_max_size());
                datagramSocket.receive(packet);
                IBencodedValue value = new Bencoding(packet.getData(), 0, packet.getLength()).decode();
                KRPC krpcPacket = new KRPC((BencodedMap) value);
                krpcPacket.validate();
                LOGGER.info("recv new packet, ip:{}, port:{}, packet:{}",
                        packet.getAddress().getHostAddress(), packet.getPort(), krpcPacket);
                if (krpcPacket.transType() == KRPC.TransType.QUERY) {
                    switch (krpcPacket.action()) {
                        case PING:
                            handlePingReq(krpcPacket, packet);
                            break;
                        case FIND_NODE:
                            handleFindNodeReq(krpcPacket, packet);
                            break;
                        case GET_PEERS:
                            handleGetPeersReq(krpcPacket, packet);
                            break;
                        case ANNOUNCE_PEER:
                            handleAnnouncePeerReq(krpcPacket, packet);
                            break;
                        default:
                            LOGGER.warn("unsuported krpc packet type, packet:{}", krpcPacket);
                            break;
                    }
                } else if (krpcPacket.transType() == KRPC.TransType.RESPONSE) {
                    Transaction transaction = transactionManager.getTransaction(krpcPacket.getTransId());
                    if (transaction == null) {
                        LOGGER.warn("unknow tranaction, maybe because timeout, packet:{}", krpcPacket);
                        continue;
                    }

                    switch (transaction.getKrpc().action()) {
                        case PING:
                            handlePingResp(krpcPacket, packet);
                            break;
                        case FIND_NODE:
                            handleFindNodeResp(krpcPacket, packet);
                            break;
                        case GET_PEERS:
                            handleGetPeersResp(krpcPacket, packet);
                            break;
                        case ANNOUNCE_PEER:
                            handleAnnouncePeerResp(krpcPacket, packet);
                            break;
                        default:
                            LOGGER.warn("unsuported krpc packet type, packet:{}", krpcPacket);
                            break;
                    }
                } else {
                    LOGGER.error("node response error, ip:{}, port:{}, packet",
                            packet.getAddress().getHostAddress(), packet.getPort(), krpcPacket);
                    System.out.println("resp is error, " + krpcPacket);
                }
            } catch (Exception e) {
                LOGGER.error("dht error", e);
            }
        }
    }

    private void handlePingReq(KRPC krpcPacket, DatagramPacket packet) {
//        Node node = new Node(packet.getAddress(), packet.getPort());

    }

    private void handlePingResp(KRPC krpcPacket, DatagramPacket packet) {
        System.out.println("ping resp:" + packet.getAddress() + ", " + krpcPacket);
    }

    private void handleFindNodeReq(KRPC krpcPacket, DatagramPacket packet) {

    }

    private void handleFindNodeResp(KRPC krpcPacket, DatagramPacket packet) {
        System.out.println("findNode resp:" + krpcPacket);
    }

    private void handleGetPeersReq(KRPC krpcPacket, DatagramPacket packet) {

    }

    private void handleGetPeersResp(KRPC krpcPacket, DatagramPacket packet) {
//        System.out.println("resp : " + krpcPacket);
        BencodedMap respData = (BencodedMap) krpcPacket.getData().get(KRPC.RESPONSE_DATA);
        if (respData.containsKey(KRPC.NODES)) {
            List<Node> nodes = JTorrentUtils.deCompactNodeInfos(respData.get(KRPC.NODES).asBytes());
            for (Node node : nodes) {
//                System.out.println(node);
                sendPingReq(node);
            }
        } else {
            BencodedList peers = (BencodedList) respData.get(KRPC.VALUES);
            for (int i = 0; i < peers.size(); i++) {
                Peer peer = JTorrentUtils.deCompactPeerInfo(peers.get(i).asBytes());
                System.out.println(peer);
            }
        }
    }

    private void handleAnnouncePeerReq(KRPC krpcPacket, DatagramPacket packet) {

    }

    private void handleAnnouncePeerResp(KRPC krpcPacket, DatagramPacket packet) {

    }
}
