package org.dochi.webserver.config;

public interface HttpReqConfig {
    int getRequestHeaderMaxSize();
    int getRequestBodyMaxSize();
}
