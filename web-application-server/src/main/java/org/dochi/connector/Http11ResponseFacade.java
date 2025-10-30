package org.dochi.connector;

import org.dochi.external.ExternalResponse;
import org.dochi.http.utils.HttpStatus;
import org.dochi.http.utils.ResponseHeaders;
import org.dochi.http.utils.DateFormatter;
import org.dochi.http.utils.HttpVersion;
import org.dochi.webserver.config.HttpResConfig;
import org.dochi.webresource.ResourceType;
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
    private TmpBufferedOutputStream bos;

    public Http11ResponseFacade(HttpResConfig httpResConfig) {
        this.httpResConfig = httpResConfig;
    }

    @Override
    public void setOutputStream(TmpBufferedOutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("OutputStream cannot be null");
        }
        this.bos = outputStream;
    }

    @Override
    public void recycle() {
        headers.clear();
        isDateHeader = true;
        isCommitted = false;
        version = HttpVersion.HTTP_1_1;
        status = HttpStatus.OK;
    }

    public ExternalResponse addVersion(HttpVersion version) {
        this.version = version;
        return this;
    }

    public ExternalResponse addHeader(String key, String value) {
        this.headers.addHeader(key, value);
        return this;
    }

    public ExternalResponse addCookie(String cookie) {
        this.headers.addHeader(ResponseHeaders.SET_COOKIE, cookie);
        return this;
    }

    public ExternalResponse addConnection(boolean isKeep) {
        this.headers.addHeader(ResponseHeaders.CONNECTION, isKeep ? "keep-alive" : "close");
        return this;
    }

    public ExternalResponse addDateHeaders(String date) {
        this.headers.addHeader(ResponseHeaders.DATE, date);
        return this;
    }

    public ExternalResponse addContentHeaders(String contentType, int contentLength) {
        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        this.headers.addContentLength(contentLength);
        return this;
    }

    public ExternalResponse inActiveDateHeader() {
        this.isDateHeader = false; return this;
    }

    public ExternalResponse activeDateHeader() {
        this.isDateHeader = true; return this;
    }

    public void send(HttpStatus status) throws IOException {
        send(status, null, null);
    }

    public void send(HttpStatus status, byte[] body, String contentType) throws IOException {
        addDefaultHeader(status, body, contentType);
        writeMessage(body);
    }

    private void addDefaultHeader(HttpStatus status, byte[] body, String contentType) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        this.status = status;

        if (this.isDateHeader && headers.getHeaders().get(ResponseHeaders.DATE) == null) {
            addDateHeaders(DateFormatter.getCurrentDate());
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

    public void sendError(HttpStatus status) throws IOException {
        sendError(status, status.getDescription());
    }

    public void sendError(HttpStatus status, String errorMessage) throws IOException {
        if (errorMessage == null) {
            errorMessage = status.getDescription();
        }
        send(status, errorMessage.getBytes(), ResourceType.TEXT.getContentType(null));
    }

    public OutputStream getOutputStream() {
        if (bos == null) {
            throw new IllegalArgumentException("OutputStream cannot be null");
        }
        return bos.getOutputStream();
    }

    private void writeMessage(byte[] body) throws IOException {
        if (bos == null) {
            throw new IllegalArgumentException("OutputStream cannot be null");
        }
        try {
            writeHeader();
            writePayload(body);
        } catch (IOException e) {
            log.error("Failed to write HTTP Response Message: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void writeHeader() throws IOException {
        bos.write(String.format("%s %d %s\r\n", version.getVersion(), status.getCode(), status.getMessage()).getBytes(StandardCharsets.ISO_8859_1));
        Set<String> keys = headers.getHeaders().keySet();
        for (String key: keys) {
            String headerLine = key + ": " + headers.getHeaders().get(key) + "\r\n";
            bos.write(headerLine.getBytes(StandardCharsets.ISO_8859_1));
        }
        bos.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    private void writePayload(byte[] body) throws IOException {
        if (body != null) {
            bos.write(body, 0, body.length);
        }
    }

    public void flush() throws IOException {
        if (!isCommitted) {
            if (bos == null) {
                throw new IllegalArgumentException("OutputStream cannot be null");
            }
            bos.flush();
            isCommitted = true;
        }
    }
}
