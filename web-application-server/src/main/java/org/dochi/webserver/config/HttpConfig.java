package org.dochi.webserver.config;

public class HttpConfig {
    private final HttpReqConfig httpReqConfig;
    private final HttpResConfig httpResConfig;

    public HttpConfig(HttpReqConfig httpReqConfig, HttpResConfig httpResConfig) {
        this.httpReqConfig = httpReqConfig;
        this.httpResConfig = httpResConfig;
    }

    public HttpReqConfig getHttpReqConfig() {
        return httpReqConfig;
    }

    public HttpResConfig getHttpResConfig() {
        return httpResConfig;
    }
}
