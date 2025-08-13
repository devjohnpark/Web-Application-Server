package org.dochi.http.response.processor;

import org.dochi.http.api.HttpApiResponse;
import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.response.HttpStatus;
import org.dochi.http.response.ResponseHeaders;
import org.dochi.http.response.stream.BufferedSocketOutputStream;
import org.dochi.http.util.DateFormatter;
import org.dochi.webresource.ResourceType;
import org.dochi.webserver.config.HttpResConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractHttpResponseProcessor implements HttpResponseProcessor {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpResponseProcessor.class);
    protected final BufferedSocketOutputStream outStream;
    protected HttpVersion version = HttpVersion.HTTP_1_1;
    protected HttpStatus status = HttpStatus.OK;
    protected final ResponseHeaders headers = new ResponseHeaders();
    protected final HttpResConfig httpResConfig;
    private boolean isDateHeader = true;
    private boolean isCommitted = false;

    protected AbstractHttpResponseProcessor(OutputStream out, HttpResConfig httpResConfig) {
        this.outStream = new BufferedSocketOutputStream(out);
        this.httpResConfig = httpResConfig;
    }

    public void recycle() {
        headers.clear();
        outStream.recycle();
        isCommitted = false;
    }

    public HttpApiResponse addVersion(HttpVersion version) {
        this.version = version;
        return this;
    }

    public HttpApiResponse addStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public HttpApiResponse addHeader(String key, String value) {
        this.headers.addHeader(key, value);
        return this;
    }

    public HttpApiResponse addCookie(String cookie) {
        this.headers.addHeader(ResponseHeaders.SET_COOKIE, cookie);
        return this;
    }

//    public HttpApiResponse addVersion(HttpVersion version) {
//        this.statusLine.setVersion(version);
//        return this;
//    }

    public HttpApiResponse addConnection(boolean isKeepAlive) {
        this.headers.addConnection(isKeepAlive);
        return this;
    }

    public HttpApiResponse addKeepAlive(int timeout, int maxRequests) {
        this.headers.addKeepAlive(timeout, maxRequests);
        return this;
    }

//    protected abstract void addStatus(HttpStatus status);

    public void addDateHeaders(String date) {
        this.headers.addHeader(ResponseHeaders.DATE, date);
    }

    public void addContentHeaders(String contentType, int contentLength) {
        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        this.headers.addContentLength(contentLength);
    }

    public void inActiveDateHeader() {
        this.isDateHeader = false;
    }

    public void activeDateHeader() {
        this.isDateHeader = true;
    }

    public void sendNoContent() throws IOException {
        send(HttpStatus.NO_CONTENT, null, null);
    }

//    public ResponseHeaders getHeaders() { return headers; }

    // 테스트 필요
    public void send(HttpStatus status) throws IOException {
//        send(status, new byte[0], null);
        send(status, null, null);
    }

    public void send(HttpStatus status, byte[] body, String contentType) throws IOException {
        addDefaultHeader(status, getContentLength(body), contentType);
        writeMessage(body);
    }

    public void sendError(HttpStatus status) throws IOException {
        sendError(status, status.getDescription());
    }

    public void sendError(HttpStatus status, String errorMessage) throws IOException {
        if (errorMessage == null) {
            errorMessage = status.getDescription();
        }
        headers.addConnection(false);
        send(status, errorMessage.getBytes(), ResourceType.TEXT.getContentType(null));
    }

    private static int getContentLength(byte[] body) {
        int contentLength = 0;
        if (body != null) {
            contentLength = body.length;
        }
        return contentLength;
    }

    private void addDefaultHeader(HttpStatus status, int contentLength, String contentType) {
        addStatus(status);
        if (isDateHeader) {
            addDateHeaders(DateFormatter.getCurrentDate());
        }
        // NO_CONTENT면, Content_Length도 없어야함
        if (status != HttpStatus.NO_CONTENT) {
            addContentHeaders(contentType, contentLength);
        }
//        // 204 No Content일 경우에만, contentType과 content length 생략
//        if (status != HttpStatus.NO_CONTENT || body != null) {
//            addContentHeaders(contentType, contentLength);
//        }
    }
    public OutputStream getOutputStream() {
        return outStream.getOutputStream();
    }

    private void writeMessage(byte[] body) throws IOException {
        try {
            writeHeader();
            writePayload(body);
        } catch (IOException e) {
            log.error("Failed to write HTTP Response Message: {}", e.getMessage(), e);
            throw e;
        }
    }

    protected abstract void writeHeader() throws IOException;
    protected abstract void writePayload(byte[] body) throws IOException;

    public void flush() throws IOException {
        if (!isCommitted) {
            outStream.flush();
            isCommitted = true;
        }
    }
}
