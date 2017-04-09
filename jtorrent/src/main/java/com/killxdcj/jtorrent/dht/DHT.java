package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.*;
import com.killxdcj.jtorrent.config.DHTConfig;
import com.killxdcj.jtorrent.peer.Peer;
import com.killxdcj.jtorrent.utils.JTorrentUtils;
import com.killxdcj.jtorrent.utils.TimeUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;

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
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10, r -> {
        Thread t = new Thread(r, "DHT_ScheduledPool");
        t.setDaemon(true);
        return t;
    });
    private RoutingTable routingTable = new RoutingTable();

    private Map<BencodedString, QueryPeersRequest> queryPeersRequestMap = new ConcurrentHashMap<>();

    public DHT(DHTConfig config) {
        this.config = config;
        nodeId = JTorrentUtils.genNodeId();
        transactionManager = new TransactionManager();
    }

    public void start() throws SocketException {
        datagramSocket = new DatagramSocket(config.getPort());
        worker.submit(this::workProc);
        pingPrimeNodes();
        startScheduleTask();
    }

    private void startScheduleTask() {
        startLogStatsSchedule();
        startFindNodeSchedule();
        startPingCheckSchedule();
        startRoutingTableRebuildSchedule();
        startQueryPeersRequestCheckSchedule();
    }

    private void startFindNodeSchedule() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info("SCHEDULE_FIND_NODE START");
            byte[] randomId = Arrays.copyOf(nodeId.asBytes(), 20);
            byte[] randomIdNext = JTorrentUtils.genByte(10);
            for (int i = 0; i < randomIdNext.length; i++) {
                randomId[10 + i] = randomIdNext[i];
            }
            BencodedString xxxx = new BencodedString(randomId);
            long startTime = TimeUtils.getCurTime();
            List<Node> nodes = routingTable.findNode(xxxx);
            for (Node node : nodes) {
                sendFindNodeReq(node, xxxx);
            }
            xxx(xxxx);
            LOGGER.info("SCHEDULE_FIND_NODE END, costtime:{}ms", TimeUtils.getElapseTime(startTime));
        }, 5000, config.getFindNodePeriod(), TimeUnit.MILLISECONDS);
    }

    private void xxx(BencodedString id) {
        for (String addr : config.getPrimeNodes()) {
            try {
                String[] ipPort = addr.split(":");
                Node node = new Node(InetAddress.getByName(ipPort[0]), Integer.parseInt(ipPort[1]));
                sendFindNodeReq(node, id);
            } catch (Exception e) {
                LOGGER.error("pingPrimeNodes error, node:{}", addr, e);
            }
        }
    }

    private void startPingCheckSchedule() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info("SCHEDULE_PING_CHECK START");
            long startTime = TimeUtils.getCurTime();
            List<Node> nodes = routingTable.getAllNode();
            List<BencodedString> nodesToRemove = new ArrayList<>();
            for (Node node : nodes) {
                long unactiveTime = TimeUtils.getElapseTime(node.getLastActive(), TimeUnit.MILLISECONDS);
                if (unactiveTime > config.getNodeMaxUnactiveTime()) {
                    nodesToRemove.add(node.getId());
                    continue;
                }
                if (unactiveTime >= config.getNodeMaxUnactiveTimeBeforePing()) {
                    sendPingReq(node);
                }
            }
            if (nodesToRemove.size() > 0) {
                LOGGER.info("SCHEDULE_PING_CHECK remove timeout node, size:{}", nodesToRemove.size());
                routingTable.removeNodes(nodesToRemove);
            }
            LOGGER.info("SCHEDULE_PING_CHECK END, costtime:{}ms", TimeUtils.getElapseTime(startTime));
        }, 0, config.getNodePingCheckPeriod(), TimeUnit.MILLISECONDS);
    }

    private void startRoutingTableRebuildSchedule() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {

        }, 0, config.getRoutingTableRebuildPerior(), TimeUnit.MILLISECONDS);
    }

    private void startLogStatsSchedule() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info("SCHEDULE_LOG_STATS START");
            long startTime = TimeUtils.getCurTime();
            routingTable.state();
            LOGGER.info("SCHEDULE_LOG_STATS END, costtime:{}ms", TimeUtils.getElapseTime(startTime));
        }, 0, 60 * 1000, TimeUnit.MILLISECONDS);
    }

    private void startQueryPeersRequestCheckSchedule() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info("SCHEDULE_QUERY_PEERS_REQUEST_CHECK START");
            long startTime = TimeUtils.getCurTime();
            for (BencodedString infohash : queryPeersRequestMap.keySet()) {
                QueryPeersRequest queryPeersRequest = queryPeersRequestMap.get(infohash);
                if (queryPeersRequest == null) continue;
                if (queryPeersRequest.aliveTime() > config.getQueryPeersRequestTimeout()
                        || queryPeersRequest.ignoreSize() > config.getQueryPeersRequestMaxqueryTimes()) {
                    // TODO notify query timeout if needed
                    queryPeersRequestMap.remove(infohash);
                    LOGGER.info("query peers over limit, infohash:{}, queryed node:{}",
                            infohash, queryPeersRequest.ignoreSize());
                } else if (queryPeersRequest.aliveTime() > config.getQueryPeersRequestMaxAliveTime()) {
                    queryPeersRequest.markStopQuery();
                    LOGGER.info("query peers over maxAlivetime, infohash:{}", infohash);
                } else {
                    LOGGER.info("query peers stats, infohash:{}, queryed node:{}, querytimes:{}", infohash.asHexString(), queryPeersRequest.ignoreSize(), queryPeersRequest.getQueryTimes());
                }
            }

            LOGGER.info("SCHEDULE_QUERY_PEERS_REQUEST_CHECK END, costtime:{}ms", TimeUtils.getElapseTime(startTime));
        }, 0, config.getQueryPeersRequestCheckPeriod(), TimeUnit.MILLISECONDS);
    }

    private void pingPrimeNodes() {
        for (String addr : config.getPrimeNodes()) {
            try {
                String[] ipPort = addr.split(":");
                Node node = new Node(InetAddress.getByName(ipPort[0]), Integer.parseInt(ipPort[1]));
                sendPingReq(node);
            } catch (Exception e) {
                LOGGER.error("pingPrimeNodes error, node:{}", addr, e);
            }
        }
    }

    public void shutdown() {
        shutdown = true;
        worker.shutdown();
        scheduledExecutorService.shutdown();
        datagramSocket.close();
    }

    public List<Peer> queryPeers(String infoHashHex) throws DecoderException {
        return queryPeers(new BencodedString(Hex.decodeHex(infoHashHex.toCharArray())));
    }

    public List<Peer> queryPeers(BencodedString infohash) {
        // TODO

        // when routingtable con't fing peer, do this
//        List<Node> nodes = routingTable.findNodeByInfoHash(infohash);
        List<Node> nodes = routingTable.getAllNode();
        QueryPeersRequest queryPeersRequest = new QueryPeersRequest(infohash);
        queryPeersRequest.ignore(nodeId);
        queryPeersRequestMap.put(infohash, queryPeersRequest);

        for (Node node : nodes) {
            queryPeersRequest.ignore(node.getId());
            queryPeersRequest.incrQueryTimes();
            sendGetPeerReq(node, infohash);
        }

        return null;
    }

    public List<Peer> queryPeers(BencodedString infohash, IDHTCallBack callBack) {
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

    private void sendPingResp(Node node, BencodedString transId) {
        try {
            KRPC krpc = KRPC.buildPingRespPacket(transId, nodeId);
            sendKrpcPacket(node, krpc);
        } catch (Exception e) {
            LOGGER.error("sendPingResp error, node:{}", node, e);
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
        while (!shutdown) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[config.getMessage_max_size()], config.getMessage_max_size());
                datagramSocket.receive(packet);
                IBencodedValue value = new Bencoding(packet.getData(), 0, packet.getLength()).decode();
                KRPC krpcPacket = new KRPC((BencodedMap) value);
                krpcPacket.validate();
                if (krpcPacket.transType() == KRPC.TransType.QUERY) {
                    handleRequest(packet, krpcPacket);
                } else if (krpcPacket.transType() == KRPC.TransType.RESPONSE) {
                    handleResponse(packet, krpcPacket);
                    continue;
                } else {
                    LOGGER.error("unknow packet, ip:{}, port:{}, packet:{}",
                            packet.getAddress().getHostAddress(), packet.getPort(), krpcPacket);
                }
            } catch (Exception e) {
                LOGGER.error("mainproc error, {}", e.getMessage(), e);
            }
        }
    }

    private void handleRequest(DatagramPacket packet, KRPC krpcPacket) throws IOException {
        LOGGER.info("recv request packet, id:{}, action:{}, ip:{}, port:{}",
                krpcPacket.getId().asHexString(), krpcPacket.action(), packet.getAddress().getHostAddress(), packet.getPort());
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
    }

    private void handleResponse(DatagramPacket packet, KRPC krpcPacket) {
        Transaction transaction = transactionManager.getTransaction(krpcPacket.getTransId());
        LOGGER.info("recv response packet, id:{}, action:{}, ip:{}, port:{}",
                krpcPacket.getId().asHexString(), transaction.getKrpc().action(), packet.getAddress().getHostAddress(), packet.getPort());
        if (transaction == null) {
            LOGGER.warn("unknow tranaction, maybe because timeout, packet:{}", krpcPacket);
            return;
        }

        switch (transaction.getKrpc().action()) {
            case PING:
                handlePingResp(transaction.getKrpc(), krpcPacket, packet);
                break;
            case FIND_NODE:
                handleFindNodeResp(transaction.getKrpc(), krpcPacket, packet);
                break;
            case GET_PEERS:
                handleGetPeersResp(transaction.getKrpc(), krpcPacket, packet);
                break;
            case ANNOUNCE_PEER:
                handleAnnouncePeerResp(transaction.getKrpc(), krpcPacket, packet);
                break;
            default:
                LOGGER.warn("unsuported krpc packet type, packet:{}", krpcPacket);
                break;
        }
    }

    private void handlePingReq(KRPC krpcPacket, DatagramPacket packet) {
        Node node = new Node(krpcPacket.getId(), packet.getAddress(), packet.getPort());
        routingTable.putNode(node);
        sendPingResp(node, krpcPacket.getTransId());
    }

    private void handlePingResp(KRPC req, KRPC resp, DatagramPacket packet) {
        Node node = new Node(resp.getId(), packet.getAddress(), packet.getPort());
        routingTable.putNode(node);
    }

    private void handleFindNodeReq(KRPC krpcPacket, DatagramPacket packet) throws IOException {
        Node node = new Node(krpcPacket.getId(), packet.getAddress(), packet.getPort());
        routingTable.putNode(node);

        BencodedString targetId = krpcPacket.getTargetId();
        if (targetId == null) {
            LOGGER.warn("get findnode request, but targetid is null, {}", krpcPacket);
            return;
        }

        List<Node> nodes = routingTable.findNode(targetId);
        KRPC resp = KRPC.buildFindNodeRespPacket(krpcPacket.getTransId(), nodeId, nodes);
        sendKrpcPacket(node, resp);
    }

    private void handleFindNodeResp(KRPC req, KRPC resp, DatagramPacket packet) {
        BencodedMap respData = (BencodedMap) resp.getData().get(KRPC.RESPONSE_DATA);
        if (!respData.containsKey(KRPC.NODES)) {
            LOGGER.error("findNode resp has no node, nodeId:{}, ip:{}", resp.getId(), packet.getAddress().getHostAddress());
            return;
        }

        List<Node> nodes = JTorrentUtils.deCompactNodeInfos(respData.get(KRPC.NODES).asBytes());
        for (Node node : nodes) {
            if (!routingTable.contains(node.getId())) {
                sendPingReq(node);
            }
        }

        for (Node node : nodes) {
            for (QueryPeersRequest queryPeersRequest: queryPeersRequestMap.values()) {
                if (!queryPeersRequest.isIgnore(node.getId())) {
                    queryPeersRequest.ignore(node.getId());
                    queryPeersRequest.incrQueryTimes();
                    sendGetPeerReq(node, queryPeersRequest.getInfohash());
                }
            }
        }
    }

    private void handleGetPeersReq(KRPC krpcPacket, DatagramPacket packet) throws IOException {
        Node node = new Node(krpcPacket.getId(), packet.getAddress(), packet.getPort());
        routingTable.putNode(node);

        BencodedString infohash = (BencodedString) krpcPacket.getData().get(KRPC.INFO_HASH);

        // TODO notify an download
        LOGGER.info("get getpeers request, infohash:{}", infohash.asHexString());

        // TODO query from routing table
        KRPC resp;
        List<Peer> peers = routingTable.findPeerByInfoHash(infohash);
        if (!peers.isEmpty()) {
            resp = KRPC.buildGetPeersRespPacketWithPeers(krpcPacket.getTransId(), nodeId, "caojian", peers);
        } else {
            List<Node> nodes = routingTable.findNodeByInfoHash(infohash);
            resp = KRPC.buildGetPeersRespPacketWithNodes(krpcPacket.getTransId(), nodeId, "caojian", nodes);
        }
        sendKrpcPacket(node, resp);
    }

    private void handleGetPeersResp(KRPC req, KRPC resp, DatagramPacket packet) {
        BencodedMap respData = (BencodedMap) resp.getData().get(KRPC.RESPONSE_DATA);
        BencodedMap reqData = (BencodedMap) req.getData().get(KRPC.QUERY_ARGS);
        if (respData.containsKey(KRPC.NODES)) {
            List<Node> nodes = JTorrentUtils.deCompactNodeInfos(respData.get(KRPC.NODES).asBytes());
            for (Node node : nodes) {
                if (!routingTable.contains(node.getId())) {
                    sendPingReq(node);
                }
            }

            BencodedString infohash = (BencodedString) reqData.get(KRPC.INFO_HASH);
            QueryPeersRequest queryPeersRequest = queryPeersRequestMap.get(infohash);
            if (queryPeersRequest != null && queryPeersRequest.isContinueQuery()) {
                for (Node node : nodes) {
                    if (!queryPeersRequest.isIgnore(node.getId())) {
                        queryPeersRequest.ignore(node.getId());
                        queryPeersRequest.incrQueryTimes();
                        sendGetPeerReq(node, infohash);
                    }
                }
            }
        } else if (respData.containsKey(KRPC.VALUES)){
            LOGGER.info("get peers result, packet:{}", resp);
            List<Peer> peers = new ArrayList<>();
            BencodedList peerList = (BencodedList) respData.get(KRPC.VALUES);
            if (peerList.size() > 0) {
                BencodedString infohash = (BencodedString) reqData.get(KRPC.INFO_HASH);
                QueryPeersRequest queryPeersRequest = queryPeersRequestMap.get(infohash);
                queryPeersRequestMap.remove(infohash);

                for (int i = 0; i < peerList.size(); i++) {
                    Peer peer = JTorrentUtils.deCompactPeerInfo(peerList.get(i).asBytes());
                    peers.add(peer);
                    LOGGER.info("get peers result, infohash:{}, peerIp:{}, peerPort:{}",
                            infohash.asHexString(), peer.getAddr().getHostAddress(), peer.getPort());
                }
                routingTable.putPeers(infohash, peers);

                if (queryPeersRequest != null && queryPeersRequest.getIdhtCallBack() != null) {
                    queryPeersRequest.getIdhtCallBack().onGetPeers(infohash, peers);
                    queryPeersRequest.markStopQuery();
                }
            }
        } else {
            LOGGER.error("invalid getpeers response packet, {}", resp);
        }
    }

    private void handleAnnouncePeerReq(KRPC krpcPacket, DatagramPacket packet) throws IOException {
        BencodedMap reqData = krpcPacket.getData();
        BencodedString infohash = (BencodedString) reqData.get(KRPC.INFO_HASH);

        Node node = new Node(krpcPacket.getId(), packet.getAddress(), packet.getPort());
        routingTable.putNode(node);

        int port = reqData.get(KRPC.PORT).asLong().intValue();
        if (reqData.containsKey(KRPC.IMPLIED_PORT) && reqData.get(KRPC.IMPLIED_PORT).asLong() != 0) {
            port = packet.getPort();
        }
        Peer peer = new Peer(packet.getAddress(), port);
        routingTable.putPeer(infohash, peer);

        LOGGER.info("get announcepeer request, infohash:{}, peer:{}", infohash, peer);

        KRPC resp = KRPC.buildAnnouncePeerRespPacket(krpcPacket.getTransId(), nodeId);
        sendKrpcPacket(node, resp);
    }

    private void handleAnnouncePeerResp(KRPC req, KRPC resp, DatagramPacket packet) {
        // TODO refresh node last active time
    }
}
