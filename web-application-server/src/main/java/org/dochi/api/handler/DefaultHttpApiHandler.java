package org.dochi.api.handler;

import org.dochi.external.ExternalResponse;
import org.dochi.http.utils.HttpStatus;
import org.dochi.external.ExternalRequest;
import org.dochi.webresource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DefaultHttpApiHandler extends HttpApiHandlerBase {
    private static final Logger log = LoggerFactory.getLogger(DefaultHttpApiHandler.class);

    @Override
    public void doGet(ExternalRequest request, ExternalResponse response) throws IOException {
        Resource resource = webResourceProvider.getResource(request.getPath());
        if (!resource.isEmpty()) {
            response.send(resource.getData(), resource.getContentType("UTF-8"));
        } else {
            response.sendError(HttpStatus.NOT_FOUND);
        }
    }
}
