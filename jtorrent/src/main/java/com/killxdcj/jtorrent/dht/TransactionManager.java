package com.killxdcj.jtorrent.dht;

import com.killxdcj.jtorrent.bencoding.BencodedString;
import com.killxdcj.jtorrent.utils.JTorrentUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 16:38
 */
public class TransactionManager {
    private Map<BencodedString, Transaction> transactionTable = new ConcurrentHashMap<>();

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
}
