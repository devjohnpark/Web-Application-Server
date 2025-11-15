package org.dochi.webserver.property;

import org.dochi.webserver.config.HttpResConfig;

public class HttpResponseProperty implements HttpResConfig {
    private static final int DEFAULT_HEADER_MAX_SIZE = 8 * 1024;
    private static final int DEFAULT_BODY_MAX_SIZE = 2 * 1024 * 1024;

    private int responseHeaderMaxSize = DEFAULT_HEADER_MAX_SIZE;
    private int responseBodyMaxSize = DEFAULT_BODY_MAX_SIZE;

    public void setResponseBodyMaxSize(int responseBodyMaxSize) {
        this.responseBodyMaxSize = responseBodyMaxSize;
    }

    public void setResponseHeaderMaxSize(int responseHeaderMaxSize) {
        this.responseHeaderMaxSize = responseHeaderMaxSize;
    }

    public int getResponsePayloadMaxSize() { return responseBodyMaxSize; }

    public int getResponseHeaderMaxSize() {
        return responseHeaderMaxSize;
    }

}
