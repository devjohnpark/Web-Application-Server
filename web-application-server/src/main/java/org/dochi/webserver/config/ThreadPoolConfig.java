package org.dochi.webserver.config;

public interface ThreadPoolConfig {
    int getMinSpareThreads();
    int getMaxThreads();
    boolean getUseVirtualThreads();
}
