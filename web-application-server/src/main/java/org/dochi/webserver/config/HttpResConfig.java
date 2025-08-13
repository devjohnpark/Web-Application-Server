package org.dochi.webserver.config;

import org.dochi.webserver.attribute.HttpResAttribute;

public class HttpResConfig {
    private final HttpResAttribute httpResAttribute;

    public HttpResConfig(HttpResAttribute httpResAttribute) {
        this.httpResAttribute = httpResAttribute;
    }

    public int getResponseHeaderMaxSize() {
        return httpResAttribute.getResponseHeaderMaxSize();
    }

    public int getResponseBodyMaxSize() {
        return httpResAttribute.getResponseBodyMaxSize();
    }
}
