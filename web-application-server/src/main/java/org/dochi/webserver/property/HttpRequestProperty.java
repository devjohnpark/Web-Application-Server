package org.dochi.webserver.property;

import org.dochi.webserver.config.HttpReqConfig;

public class HttpRequestProperty implements HttpReqConfig {
    private static final int DEFAULT_HEADER_MAX_SIZE = 8 * 1024;
    private static final int DEFAULT_BODY_MAX_SIZE = 2 * 1024 * 1024;

    private int requestHeaderMaxSize = DEFAULT_HEADER_MAX_SIZE;
    private int requestPayloadMaxSize = DEFAULT_BODY_MAX_SIZE;

    public void setRequestPayloadMaxSize(int requestPayloadMaxSize) {
        this.requestPayloadMaxSize = requestPayloadMaxSize;
    }

    public void setRequestHeaderMaxSize(int requestHeaderMaxSize) {
        this.requestHeaderMaxSize = requestHeaderMaxSize;
    }

    public int getRequestPayloadMaxSize() {
        return requestPayloadMaxSize;
    }

    public int getRequestHeaderMaxSize() {
        return requestHeaderMaxSize;
    }
}
