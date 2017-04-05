package com.killxdcj.jtorrent.config;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/04
 * Time: 16:03
 */
public class DHTConfig {
    private int message_max_size = 10 * 1024 * 1024;
    private int port = 6881;

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
}
