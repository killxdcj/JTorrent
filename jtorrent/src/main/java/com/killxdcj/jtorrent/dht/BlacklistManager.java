package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/11
 * Time: 22:19
 */
public class BlacklistManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlacklistManager.class);

    private Map<String/* ip */, Long/* expired time */> blockedIpTable = new ConcurrentHashMap<>();
    private Map<String/* net segment */, Long/* expired time */> blockedNetSegMentTable = new ConcurrentHashMap<>();
    private Map<String/* ip */, Long/* stain count */> ipStainTable = new ConcurrentHashMap<>();

    private static final int IP_BLOCK_THRESHOLD = 10;
    private static final int SEGMENT_BLOCK_THRESHOLD = 10;
    private static final int IP_BLOCK_EXPIRED_TIME = 10 * 60 * 1000;
    private static final int SEGMENT_BLOCK_EXPIRED_TIME = 30 * 60 * 1000;
    private static final int BLOCK_EXPIRE_CHECK_PERIOD = 1 * 60 * 1000;
    private static final int BLOCKED_SEGMENT_BUILD_PERIOD = 30 * 1000;
    private static final int LOG_STATS_PERIOD = 60 * 1000;

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5, r -> {
       Thread t = new Thread(r, "BlacklistManager schedule");
       t.setDaemon(true);
       return t;
    });

    public void start() {
        startBlockedTableBuildSchedule();
        startBlockExpireCheckSchedule();
        startLogStatsSchedule();
    }

    public void shutdown() {
        scheduledExecutorService.shutdown();
    }

    private void startBlockExpireCheckSchedule() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info("BlockExpireCheckSchedule start");
            long curTime = TimeUtils.getCurTime();
            Map<String, Long> blockedIpTableNew = new HashMap<>();
            blockedIpTable.forEach((ip, expiredTime) -> {
                if (curTime < expiredTime) blockedIpTableNew.put(ip, expiredTime);
            });
            blockedIpTable = blockedIpTableNew;

            Map<String, Long> blockedNetSegMentTableNew = new HashMap<>();
            blockedNetSegMentTable.forEach((segment, expiredTime) -> {
                if (curTime < expiredTime) blockedIpTableNew.put(segment, expiredTime);
            });
            blockedNetSegMentTable = blockedNetSegMentTableNew;

            LOGGER.info("BlockExpireCheckSchedule end, costtime: {} ms", TimeUtils.getElapseTime(curTime));
        }, 5, BLOCK_EXPIRE_CHECK_PERIOD, TimeUnit.MILLISECONDS);
    }

    private void startBlockedTableBuildSchedule() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info("BlockedTableBuildSchedule start");
            long startTime = TimeUtils.getCurTime();
            Map<String, Long> ipStainTableNew = new HashMap<>();
            ipStainTable.forEach((ip, stain) -> {
                if (stain > IP_BLOCK_THRESHOLD) {
                    blockedIpTable.put(ip, TimeUtils.getExpiredTime(IP_BLOCK_EXPIRED_TIME));
                } else {
                    ipStainTableNew.put(ip, stain);
                }
            });
            ipStainTable = ipStainTableNew;

            Map<String, Integer> segmentStainTable = new HashMap<>();
            blockedIpTable.keySet().forEach(ip -> {
                String segment = cutSegMent(ip);
                segmentStainTable.put(segment, segmentStainTable.getOrDefault(segment, 0) + 1);
            });
            ipStainTable.keySet().forEach(ip -> {
                String segment = cutSegMent(ip);
                segmentStainTable.put(segment, segmentStainTable.getOrDefault(segment, 0) + 1);
            });
            segmentStainTable.forEach((segment, stain) -> {
                if (stain > SEGMENT_BLOCK_THRESHOLD && !blockedNetSegMentTable.containsKey(segment)) {
                    blockedNetSegMentTable.put(segment, TimeUtils.getExpiredTime(SEGMENT_BLOCK_EXPIRED_TIME));
                }
            });
            LOGGER.info("BlockedTableBuildSchedule end, costtime: {} ms", TimeUtils.getElapseTime(startTime));
        }, 0, BLOCKED_SEGMENT_BUILD_PERIOD, TimeUnit.MILLISECONDS);
    }

    private void startLogStatsSchedule() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info("BlockedTableBuildSchedule start");
            long startTime = TimeUtils.getCurTime();
            LOGGER.info("blacklist stats, blockedIp:{}, blockedSegment:{}, ipStain:{}, blockedSegment list:{}",
                    blockedIpTable.size(), blockedNetSegMentTable.size(), ipStainTable.size(), blockedNetSegMentTable.values());
            LOGGER.info("BlockedTableBuildSchedule end, costtime:{}ms", TimeUtils.getElapseTime(startTime));
        }, 0, LOG_STATS_PERIOD, TimeUnit.MILLISECONDS);
    }

    public void markBad(String ip) {
        blockedIpTable.put(ip, TimeUtils.getExpiredTime(IP_BLOCK_EXPIRED_TIME));
    }

    public void markGood(String ip) {
        blockedIpTable.remove(ip);
        ipStainTable.remove(ip);
    }

    public void markStain(String ip) {
        ipStainTable.put(ip, ipStainTable.getOrDefault(ip, 0L) + 1);
    }

    public boolean isBlocked(String ip) {
        if (blockedIpTable.containsKey(ip)) return true;
        if (blockedNetSegMentTable.containsKey(cutSegMent(ip))) return true;
        return false;
    }

    private String cutSegMent(String ip) {
        int idx = ip.lastIndexOf('.');
        if (idx == -1) return ip;
        return ip.substring(0, idx);
    }
}
