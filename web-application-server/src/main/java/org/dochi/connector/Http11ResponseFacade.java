package org.dochi.connector;

import org.dochi.external.ExternalResponse;
import org.dochi.http.utils.DateFormatter;
import org.dochi.http.utils.HttpStatus;
import org.dochi.http.utils.ResponseHeaders;
import org.dochi.http.utils.HttpVersion;
import org.dochi.webresource.ResourceType;
import org.dochi.webserver.config.HttpResConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Http11ResponseFacade implements InternalResponse, ExternalResponse {
    private static final Logger log = LoggerFactory.getLogger(Http11ResponseFacade.class);
    private HttpVersion version = HttpVersion.HTTP_1_1;
    private HttpStatus status = HttpStatus.OK;
    private final ResponseHeaders headers = new ResponseHeaders();
    private final HttpResConfig httpResConfig;
    private boolean isDateHeader = true;
    private boolean isCommitted = false;
    private OutputStream out;

    public Http11ResponseFacade(HttpResConfig httpResConfig) {
        this.httpResConfig = httpResConfig;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException("OutputStream cannot be null");
        }
        this.out = out;
    }

    @Override
    public void recycle() {
        headers.clear();
        isDateHeader = true;
        isCommitted = false;
        version = HttpVersion.HTTP_1_1;
        status = HttpStatus.OK;
    }

    public ExternalResponse setStatus(HttpStatus status) {
        checkCommit();
        if (status == null) { throw new IllegalArgumentException("HTTP Response Status cannot be null"); }
        this.status = status;
        return this;
    }

    public ExternalResponse setHeader(String key, String value) {
        checkCommit();
        this.headers.addHeader(key, value);
        return this;
    }

    public ExternalResponse setCookie(String cookie) {
        checkCommit();
        this.headers.addHeader(ResponseHeaders.SET_COOKIE, cookie);
        return this;
    }

    public ExternalResponse setConnection(boolean isKeep) {
        checkCommit();
        this.headers.addHeader(ResponseHeaders.CONNECTION, isKeep ? "keep-alive" : "close");
        return this;
    }

    public ExternalResponse setContentType(String contentType) {
        checkCommit();
        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        return this;
    }

    public ExternalResponse setContentLength(int contentLength) {
        checkCommit();
        this.headers.addContentLength(contentLength);
        return this;
    }

    public ExternalResponse setDateHeader(String date) {
        checkCommit();
        this.headers.addHeader(ResponseHeaders.DATE, date);
        return this;
    }

    public ExternalResponse inActiveDateHeader() {
        this.isDateHeader = false; return this;
    }

    public ExternalResponse activeDateHeader() {
        this.isDateHeader = true; return this;
    }

    private void checkCommit() {
        if(isCommitted) throw new IllegalStateException("Cannot add header after header is commited");
    }

    public void send(HttpStatus status) throws IOException {
        send(status, null, null);
    }

    public void send(HttpStatus status, byte[] body, String contentType) throws IOException {
        addDefaultHeader(status, body, contentType);
        commitMessage(body);
    }

    public void sendError(HttpStatus status) throws IOException {
        sendError(status, status.getDescription());
    }

    public void sendError(HttpStatus status, String errorMessage) throws IOException {
        if (status.getCode() < 400) throw new IllegalStateException("HTTP status code is not error" + status.getCode());;
        if (errorMessage == null) {
            errorMessage = status.getDescription();
        }
        send(status, errorMessage.getBytes(), ResourceType.TEXT.getContentType(null));
    }

    private void addDefaultHeader(HttpStatus status, byte[] body, String contentType) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        this.status = status;

        if (this.isDateHeader && headers.getHeaders().get(ResponseHeaders.DATE) == null) {
            setDateHeader(DateFormatter.getCurrentDate());
        }

        if (status != HttpStatus.NO_CONTENT) {
            // content 헤더 설정 안된 경우만 추가
            if (headers.getContentLength() <= 0 && body != null) {
                this.headers.addContentLength(body.length);
            }
            if (headers.getHeaders().get(ResponseHeaders.CONTENT_TYPE) == null && contentType != null) {
                this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
            }
        }
    }

    public OutputStream getOutputStream() throws IOException {
        commitMessage(null);
        return out;
    }

    private void commitMessage(byte[] body) throws IOException {
        if (isCommitted) return;
        writeHeader();
        writeBody(body);
        isCommitted = true;
    }

    public void flush() throws IOException {
        if (isCommitted) {
            checkOutputStream();
            out.flush();
        }
    }

    private void writeHeader() throws IOException {
        checkOutputStream();
        out.write(String.format("%s %d %s\r\n", version.getVersion(), status.getCode(), status.getMessage()).getBytes(StandardCharsets.ISO_8859_1));
        Set<String> keys = headers.getHeaders().keySet();
        for (String key: keys) {
            String headerLine = key + ": " + headers.getHeaders().get(key) + "\r\n";
            out.write(headerLine.getBytes(StandardCharsets.ISO_8859_1));
        }
        out.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    private void writeBody(byte[] body) throws IOException {
        checkOutputStream();
        if (body != null) {
            out.write(body, 0, body.length);
        }
    }

    private void checkOutputStream() {
        if (out == null) {
            throw new IllegalStateException("OutputStream cannot be null");
        }
    }
}