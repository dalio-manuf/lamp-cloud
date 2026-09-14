package com.dalio.cloud.file.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 文件锁工具类
 *
 * @author admin
 * @date 2019-06-14
 */
public final class FileLock {
    private static final Map<String, Lock> LOCKS = new ConcurrentHashMap<>(16);

    private FileLock() {
    }

    /**
     * 获取锁
     *
     * @param key key
     * @return java.util.concurrent.locks.Lock
     * @author admin
     * @date 2019-06-14 11:30
     */
    public static Lock getLock(String key) {
        return LOCKS.computeIfAbsent(key, k -> new ReentrantLock());
    }

    /**
     * 删除锁
     *
     * @param key keu
     */
    public static void removeLock(String key) {
        LOCKS.remove(key);
    }
}
