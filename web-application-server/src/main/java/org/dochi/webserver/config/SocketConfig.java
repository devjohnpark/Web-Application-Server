package org.dochi.webserver.config;

public interface SocketConfig {
    int getConnectionTimeout();
    int getKeepAliveTimeout();
    int getMaxKeepAliveRequests();
}
