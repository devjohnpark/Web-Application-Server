package org.dochi.http.buffer.api;

import org.dochi.http.api.HttpApiResponse;
import org.dochi.webserver.config.WebServiceConfig;

import java.io.IOException;

public interface HttpApiHandler {
    void init(WebServiceConfig config);
    void service(HttpApiRequest request, HttpApiResponse response) throws IOException;
    void destroy();
}
