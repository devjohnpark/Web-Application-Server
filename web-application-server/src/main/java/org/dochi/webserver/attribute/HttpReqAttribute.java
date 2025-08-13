package org.dochi.webserver.attribute;

public class HttpReqAttribute {
    private static final int DEFAULT_HEADER_MAX_SIZE = 8192;
    private static final int DEFAULT_BODY_MAX_SIZE = 2097152;

    private int requestHeaderMaxSize = DEFAULT_HEADER_MAX_SIZE;
    private int requestBodyMaxSize = DEFAULT_BODY_MAX_SIZE;

    public void setRequestBodyMaxSize(int requestBodyMaxSize) {
        this.requestBodyMaxSize = requestBodyMaxSize;
    }

    public void setRequestHeaderMaxSize(int requestHeaderMaxSize) {
        this.requestHeaderMaxSize = requestHeaderMaxSize;
    }

    public int getRequestBodyMaxSize() {
        return requestBodyMaxSize;
    }

    public int getRequestHeaderMaxSize() {
        return requestHeaderMaxSize;
    }
}
