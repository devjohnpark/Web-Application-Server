package org.dochi.api.handler;

import org.dochi.external.ExternalRequest;
import org.dochi.external.ExternalResponse;
import org.dochi.http.utils.HttpStatus;
import org.dochi.webresource.WebResourceProvider;
import org.dochi.webserver.config.WebServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractHttpApiHandler implements HttpApiHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpApiHandler.class);

    protected WebResourceProvider webResourceProvider = null;

    @Override
    public void init(WebServiceConfig config) {
        this.webResourceProvider = config.getWebResourceProvider();
        log.info("{} init", this.getClass().getSimpleName());
    }

    @Override
    public void destroy() {
        if (this.webResourceProvider != null) {
            this.webResourceProvider.close();
        }
        log.info("{} destroy", this.getClass().getSimpleName());
    }

    @Override
    public void service(ExternalRequest request, ExternalResponse response) throws IOException {
        String method = request.getMethod();
        if (method.equalsIgnoreCase("GET")) {
            doGet(request, response);
        } else if (method.equalsIgnoreCase("POST")) {
            doPost(request, response);
        } else if (method.equalsIgnoreCase("PUT")) {
            doPut(request, response);
        }  else if (method.equalsIgnoreCase("PATCH")) {
            doPatch(request, response);
        } else if (method.equalsIgnoreCase("DELETE")) {
            doDelete(request, response);
        } else {
            response.sendError(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    protected void doGet(ExternalRequest request, ExternalResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    protected void doPost(ExternalRequest request, ExternalResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    protected void doPut(ExternalRequest request, ExternalResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    protected void doPatch(ExternalRequest request, ExternalResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    protected void doDelete(ExternalRequest request, ExternalResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    private void sendDefaultError(ExternalRequest request, ExternalResponse response) throws IOException {
        String protocol = request.getProtocol();
        String errorMessage = String.format("http method %s not supported", request.getMethod());
        // PUT, PATCH, DELETE, OPTIONS 등은 HTTP/0.9나 HTTP/1.0에서 명시적으로 정의되어 있지 않는다.
        if (protocol.equalsIgnoreCase("HTTP/0.9") || protocol.equalsIgnoreCase("HTTP/1.0")) {
            response.sendError(HttpStatus.BAD_REQUEST, errorMessage);
        } else {
            response.sendError(HttpStatus.METHOD_NOT_ALLOWED, errorMessage);
        }
    }
}
