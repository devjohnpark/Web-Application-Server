package org.dochi.connector;

import org.dochi.api.handler.HttpApiHandler;
import org.dochi.webserver.attribute.WebService;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.config.HttpReqConfig;
import org.dochi.webserver.config.HttpResConfig;

import java.util.Map;

public class Connector {
    private final WebService webService;
    private final HttpConfig httpConfig;
//    private final HttpReqConfig httpReqConfig;
//    private final HttpResConfig httpResConfig;

    public Connector(WebService webService, HttpConfig httpConfig) {
        this.webService = webService;
        this.httpConfig = httpConfig;
//        this.httpReqConfig = reqConfig;
//        this.httpResConfig = resConfig;
    }

    public Map<String, HttpApiHandler> getServices() {
        return webService.getServices();
    }

    public HttpResConfig getHttpResConfig() {
        return httpConfig.getHttpResConfig();
    }

    public HttpReqConfig getHttpReqConfig() {
        return httpConfig.getHttpReqConfig();
    }
}
