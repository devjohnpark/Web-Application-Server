package org.dochi.webserver.attribute;

public class HttpResAttribute {
    private static final int DEFAULT_HEADER_MAX_SIZE = 8192;
    private static final int DEFAULT_BODY_MAX_SIZE = 2097152;

    private int responseHeaderMaxSize = DEFAULT_HEADER_MAX_SIZE;
    private int responseBodyMaxSize = DEFAULT_BODY_MAX_SIZE;

    public void setResponseBodyMaxSize(int responseBodyMaxSize) {
        this.responseBodyMaxSize = responseBodyMaxSize;
    }

    public void setResponseHeaderMaxSize(int responseHeaderMaxSize) {
        this.responseHeaderMaxSize = responseHeaderMaxSize;
    }


    public int getResponseBodyMaxSize() {
        return responseBodyMaxSize;
    }

    public int getResponseHeaderMaxSize() {
        return responseHeaderMaxSize;
    }
}
