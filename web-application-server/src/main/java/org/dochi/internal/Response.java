package org.dochi.internal;

import org.dochi.http.utils.DateFormatter;
import org.dochi.http.utils.HttpStatus;
import org.dochi.http.utils.HttpVersion;
import org.dochi.http.utils.ResponseHeaders;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

// Low-level Response
// 한 번 커밋되면 메세지 변경 불가
// OutputStream은 내부 요청 처리자가 주입
public final class Response implements ResponseContext {
    private HttpVersion version = HttpVersion.HTTP_1_1;
    private HttpStatus status = HttpStatus.OK;
    private final ResponseHeaders headers = new ResponseHeaders();
    private boolean committed = false;
    private OutputStream out;

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void recycle() {
        headers.clear();
        version = HttpVersion.HTTP_1_1;
        status = HttpStatus.OK;
        committed = false;
    }

    public HttpVersion getVersion() { return version; }

    public HttpStatus getStatus() { return status; }

    public ResponseHeaders getHeaders() { return headers; }

    public boolean isCommitted() { return committed; }

    public void setVersion(HttpVersion version) {
        checkNotCommitted();
        if (version == null) throw new IllegalArgumentException("HttpVersion cannot be null");
        this.version = version;
    }

    public void setStatus(HttpStatus status) {
        checkNotCommitted();
        if (status == null) throw new IllegalArgumentException("HttpStatus cannot be null");
        this.status = status;
    }

    public void addHeader(String key, String value) {
        checkNotCommitted();
        headers.addHeader(key, value);
    }

    public void setKeepAlive(String value) {
        checkNotCommitted();
        headers.addHeader(ResponseHeaders.KEEP_ALIVE, value);
    }

    public void setCookie(String cookie) {
        checkNotCommitted();
        headers.addHeader(ResponseHeaders.SET_COOKIE, cookie);
    }

    public void setContentType(String contentType) {
        checkNotCommitted();
        headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
    }

    public void setConnection(String connection) {
        checkNotCommitted();
        headers.addHeader(ResponseHeaders.CONNECTION, connection);
    }

    public void setContentLength(int len) {
        checkNotCommitted();
        headers.addContentLength(len);
    }

    public void setDate(String date) {
        checkNotCommitted();
        headers.addHeader(ResponseHeaders.DATE, date);
    }

    public void commit() throws IOException {
        commit(null);
    }

    private void commit(byte[] body) throws IOException {
        if (committed) return;
        onCommit(body);
        committed = true;
    }

    public void commitMessage(byte[] body) throws IOException {
        commit(body);
    }

    public void flush() throws IOException {
        if (!committed) return;
        out.flush();
    }

    private ResponseFacade facade;

    public ResponseFacade getFacade() {
        return this.facade;
    }

    public void setFacade(ResponseFacade facade) {
        this.facade = facade;
        if (this.facade == null) {
            throw new IllegalStateException("Facade cannot be null");
        }
        if (this.out == null) {
            throw new IllegalStateException("Output stream cannot be null");
        }
        facade.setOutputStream(this.out);
    }

    private void onCommit(byte[] body) throws IOException {
        writeHeader();
        writeBody(body);
    }

    private void setDefaultDate() {
        String date = headers.getDate();
        if (date == null) {
            setDate(DateFormatter.getCurrentDate());
        }
    }

    private void checkNotCommitted() {
        if (committed) throw new IllegalStateException("Cannot set headers after committed");
    }


    private void writeHeader() throws IOException {
        setDefaultDate();
        writeStatusLine();
        writeHeaders();
    }

    private void writeStatusLine() throws IOException {
        out.write(String.format("%s %d %s\r\n",
                version.getVersion(),
                status.getCode(),
                status.getMessage()
        ).getBytes(StandardCharsets.ISO_8859_1));
    }

    private void writeHeaders() throws IOException {
        Set<String> keys = headers.getHeaders().keySet();
        for (String key : keys) {
            String headerLine = key + ": " + headers.getHeaders().get(key) + "\r\n";
            out.write(headerLine.getBytes(StandardCharsets.ISO_8859_1));
        }
        out.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    private void writeBody(byte[] body) throws IOException {
        if (isNotEmptyBody(body)) {
            out.write(body, 0, body.length);
        }
    }

    private boolean isNotEmptyBody(byte[] body) {
        return body != null && body.length > 0;
    }
}

