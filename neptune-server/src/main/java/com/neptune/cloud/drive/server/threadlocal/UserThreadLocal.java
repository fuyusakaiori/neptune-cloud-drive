package com.neptune.cloud.drive.server.threadlocal;

/**
 * 传递用户 ID
 */
public class UserThreadLocal {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置用户 ID
     */
    public static void set(long userId) {
        threadLocal.set(userId);
    }

    /**
     * 获取用户 ID
     */
    public static Long get() {
        return threadLocal.get();
    }

}

