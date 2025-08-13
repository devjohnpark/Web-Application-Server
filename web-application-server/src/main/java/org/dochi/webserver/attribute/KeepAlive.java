package org.dochi.webserver.attribute;

public class KeepAlive {
    private static final int DEFAULT_KEEP_ALIVE_TIMEOUT = 20000; // default: 20000 ms
    private static final int DEFAULT_MAX_REQUESTS = 100; // default: 100

    private int keepAliveTimeout = DEFAULT_KEEP_ALIVE_TIMEOUT;
    private int maxKeepAliveRequests = DEFAULT_MAX_REQUESTS;

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        if (keepAliveTimeout <= 0) {
            throw new IllegalArgumentException("keepAliveTimeout cannot be negative or zero.");
        }
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public void setMaxKeepAliveRequests(int maxKeepAliveRequests) {
        if (maxKeepAliveRequests <= 0) {
            throw new IllegalArgumentException("maxKeepAliveRequests cannot be negative or zero.");
        }
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }
}
