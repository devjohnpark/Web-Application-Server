package org.dochi.webserver.config;

import org.dochi.webserver.attribute.HttpReqAttribute;

public class HttpReqConfig {
    private final HttpReqAttribute httpReqAttribute;

    public HttpReqConfig(HttpReqAttribute httpReqAttribute) {
        this.httpReqAttribute = httpReqAttribute;
    }

    public int getRequestHeaderMaxSize() {
        return httpReqAttribute.getRequestHeaderMaxSize();
    }

    public int getRequestBodyMaxSize() {
        return httpReqAttribute.getRequestBodyMaxSize();
    }
}
