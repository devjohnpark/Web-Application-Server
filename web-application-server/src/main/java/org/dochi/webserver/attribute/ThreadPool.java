package org.dochi.webserver.attribute;

import org.dochi.webserver.config.ThreadPoolConfig;

public class ThreadPool implements ThreadPoolConfig {
    private static final int DEFAULT_CORE_POOL_SIZE = 500; // default 1000
    private static final int DEFAULT_MAX_POOL_SIZE = 2000; // default 20000

    private int minSpareThreads = DEFAULT_CORE_POOL_SIZE;
    private int maxThreads = DEFAULT_MAX_POOL_SIZE;

    public void setMinSpareThreads(int minSpareThreads) {
        if (minSpareThreads < 1) {
            throw new IllegalArgumentException("thread pool size cannot be less than 1");
        }
        this.minSpareThreads = minSpareThreads;
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    public void setMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            throw new IllegalArgumentException("thread pool size cannot be less than 1");
        }
        this.maxThreads = maxThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }
}
