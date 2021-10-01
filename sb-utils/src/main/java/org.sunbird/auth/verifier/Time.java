package org.sunbird.auth.verifier;

import java.util.Date;

public class Time {
    private static int offset;

    public Time() {
    }

    public static int currentTime() {
        return (int)(System.currentTimeMillis() / 1000L) + offset;
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis() + (long)(offset * 1000);
    }

    public static Date toDate(int time) {
        return new Date((long)time * 1000L);
    }

    public static Date toDate(long time) {
        return new Date(time);
    }

    public static long toMillis(int time) {
        return (long)time * 1000L;
    }

    public static int getOffset() {
        return offset;
    }

    public static void setOffset(int offset) {offset = offset;
    }
}