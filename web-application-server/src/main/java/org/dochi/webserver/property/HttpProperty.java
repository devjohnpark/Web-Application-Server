package org.dochi.webserver.property;

import org.dochi.webserver.config.HttpConfig;


public class HttpProperty implements HttpConfig {
    private final HttpRequestProperty httpReqConfig;
    private final HttpResponseProperty httpResConfig;

    public HttpProperty(HttpRequestProperty httpReqConfig, HttpResponseProperty httpResConfig) {
        this.httpReqConfig = httpReqConfig;
        this.httpResConfig = httpResConfig;
    }

    public HttpRequestProperty getHttpReqConfig() {
        return httpReqConfig;
    }

    public HttpResponseProperty getHttpResConfig() {
        return httpResConfig;
    }
}
