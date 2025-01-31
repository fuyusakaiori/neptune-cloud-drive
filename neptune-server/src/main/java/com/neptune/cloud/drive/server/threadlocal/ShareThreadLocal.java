package com.neptune.cloud.drive.server.threadlocal;

public class ShareThreadLocal {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置用户 ID
     */
    public static void set(long shareId) {
        threadLocal.set(shareId);
    }

    /**
     * 获取用户 ID
     */
    public static Long get() {
        return threadLocal.get();
    }

}
