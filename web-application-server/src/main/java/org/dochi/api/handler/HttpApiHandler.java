package org.dochi.api.handler;

import org.dochi.external.ExternalRequest;
import org.dochi.external.ExternalResponse;
import org.dochi.webserver.config.WebServiceConfig;

import java.io.IOException;

//  DB Connection, file, etc external resources close after using: init -> destroy
public interface HttpApiHandler {
    void init(WebServiceConfig config);
    void service(ExternalRequest request, ExternalResponse response) throws IOException;
    void destroy();
}
