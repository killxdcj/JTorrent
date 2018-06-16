package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.utils.JTorrentUtils;
import com.killxdcj.jtorrent.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 16:38
 */
public class TransactionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);

    private static final int TRANSACTION_EXPIRED_CHECK_PERIOD = 5 * 60 * 1000;

    private ITransactionStatsNotify notify;
    private Map<BencodedString, Transaction> transactionTable = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5, r -> {
        Thread thread = new Thread(r, "TransactionManager schedule");
        thread.setDaemon(true);
        return thread;
    });

    public TransactionManager() {
    }

    public TransactionManager(ITransactionStatsNotify notify) {
        this.notify = notify;
    }

    public void start() {
        startTransactionExpiredCheckSchedule();
    }

    public void shutdown() {
        scheduledExecutorService.shutdown();
    }

    private void startTransactionExpiredCheckSchedule() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info("TransactionExpiredCheckSchedule start");
            long startTime = TimeUtils.getCurTime();

            List<Transaction> expiredTrans = new ArrayList<>();
            Map<BencodedString, Transaction> transactionTableNew = new ConcurrentHashMap<>();
            transactionTable.forEach((transId, transAction) -> {
                if (transAction.isExpired()) {
                    expiredTrans.add(transAction);
                } else {
                    transactionTableNew.put(transId, transAction);
                }
            });
            transactionTable = transactionTableNew;

            if (notify != null) {
                scheduledExecutorService.submit(() -> {
                    notify.onTransactionExpired(expiredTrans);
                });
            }
            LOGGER.info("TransactionExpiredCheckSchedule end, costtime: {} ms", TimeUtils.getElapseTime(startTime));
        }, 0, TRANSACTION_EXPIRED_CHECK_PERIOD, TimeUnit.MILLISECONDS);
    }

    public static BencodedString genTransactionId() {
        return new BencodedString(JTorrentUtils.genByte(2));
    }

    public void putTransaction(Transaction transaction) {
        transactionTable.put(transaction.getKrpc().getTransId(), transaction);
    }

    public Transaction getTransaction(BencodedString transId) {
        return transactionTable.get(transId);
    }

    public boolean containsKey(BencodedString transId) {
        return transactionTable.containsKey(transId);
    }

    public interface ITransactionStatsNotify {
        void onTransactionExpired(List<Transaction> transactions);
    }
}
