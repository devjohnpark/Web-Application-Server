package org.dochi.webserver.attribute;

import org.dochi.webserver.config.SocketConfig;

public class SocketAttribute implements SocketConfig {
    private static final int DEFAULT_KEEP_ALIVE_TIMEOUT = 20000; // default: 20000 ms
    private static final int DEFAULT_MAX_REQUESTS = 50; // default: 100

    private int keepAliveTimeout = DEFAULT_KEEP_ALIVE_TIMEOUT;
    private int maxKeepAliveRequests = DEFAULT_MAX_REQUESTS;
    private int connectTimeout = 0;

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }

    public int getConnectTimeout() { return connectTimeout; }

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

    public void setConnectTimeout(int connectTimeout) {
        if (connectTimeout <= 0) {
            throw new IllegalArgumentException("connectTimeout cannot be negative or zero.");
        }
        this.connectTimeout = connectTimeout;
    }

    @Override
    public int getConnectionTimeout() {
        return connectTimeout == 0 ? keepAliveTimeout : connectTimeout;
    }
}
