package org.dochi.webserver.attribute;

public class ThreadPool {
    private static final int DEFAULT_CORE_POOL_SIZE = 10; // default 10
    private static final int DEFAULT_MAX_POOL_SIZE = 200; // default 200

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
