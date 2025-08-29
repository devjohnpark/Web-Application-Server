package org.dochi.api.handler;

import org.dochi.external.HttpExternalRequest;
import org.dochi.external.HttpExternalResponse;
import org.dochi.webserver.config.WebServiceConfig;

import java.io.IOException;

public interface HttpApiHandler {
    void init(WebServiceConfig config);
    void service(HttpExternalRequest request, HttpExternalResponse response) throws IOException;
    void destroy();
}
