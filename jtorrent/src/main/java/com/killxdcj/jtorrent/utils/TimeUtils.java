package com.killxdcj.jtorrent.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: caojianhua
 * Date: 2017/04/07
 * Time: 20:25
 */
public class TimeUtils {
    public static long getCurTime() {
        return System.nanoTime();
    }

    public static long getElapseTime(long preTime) {
        return getElapseTime(preTime, TimeUnit.MILLISECONDS);
    }

    public static long getExpiredTime(long expired) {
        return getCurTime() + expired;
    }

    public static long getElapseTime(long preTime, TimeUnit timeUnit) {
        long duration = System.nanoTime() - preTime;
        switch (timeUnit) {
            case DAYS:
                return TimeUnit.NANOSECONDS.toDays(duration);
            case HOURS:
                return TimeUnit.NANOSECONDS.toHours(duration);
            case MINUTES:
                return TimeUnit.NANOSECONDS.toMinutes(duration);
            case SECONDS:
                return TimeUnit.NANOSECONDS.toSeconds(duration);
            case MILLISECONDS:
                return TimeUnit.NANOSECONDS.toMillis(duration);
            case MICROSECONDS:
                return TimeUnit.NANOSECONDS.toMicros(duration);
            default:
                return duration;
        }
    }
}