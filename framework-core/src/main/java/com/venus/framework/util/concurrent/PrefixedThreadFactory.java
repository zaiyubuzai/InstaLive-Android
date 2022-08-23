package com.venus.framework.util.concurrent;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程名的ThreadFactory
 *
 * @see {@link java.util.concurrent.Executors#DefaultThreadFactory}
 */
public class PrefixedThreadFactory implements ThreadFactory {
    private static final Map<String, AtomicInteger> poolNumberMap = new HashMap<>();
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public PrefixedThreadFactory(@NonNull String poolPrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();

        AtomicInteger poolNum = poolNumberMap.get(poolPrefix);
        if (poolNum == null) {
            poolNum = new AtomicInteger(1);
            poolNumberMap.put(poolPrefix, poolNum);
        }

        this.namePrefix = poolPrefix + "-" + poolNum.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread t = new Thread(group,
                r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }

        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
