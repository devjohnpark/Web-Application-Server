package org.dochi.connector;

import org.dochi.external.ExternalResponse;
import org.dochi.http.utils.HttpStatus;
import org.dochi.internal.ResponseFacade;
import org.dochi.webresource.ResourceType;
import org.dochi.webserver.config.HttpResConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

// 개발자가 HTTP API에서 응답을 보내기 쉽도록 send 메서드로만 호출
public class Response extends ResponseFacade implements ExternalResponse {
    private final HttpResConfig httpResConfig;
    private OutputStream out;

    public Response(org.dochi.internal.Response response, HttpResConfig httpResConfig) {
        super(response);
        this.httpResConfig = httpResConfig;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void recycle() {
        // not completed this obj, will write the code
    }

    public ExternalResponse setStatus(HttpStatus status) {
        response.setStatus(status);
        return this;
    }

    public ExternalResponse setHeader(String key, String value) {
        response.addHeader(key, value);
        return this;
    }

    public ExternalResponse setCookie(String cookie) {
        response.setCookie(cookie);
        return this;
    }

    public ExternalResponse setConnection(String connection) {
        response.setConnection(connection);
        return this;
    }

    public ExternalResponse setContentType(String contentType) {
        response.setContentType(contentType);
        return this;
    }

    public ExternalResponse setContentLength(int contentLength) {
        response.setContentLength(contentLength);
        return this;
    }

    public OutputStream getOutputStream() throws IOException {
        response.commit();
        return this.out;
    }

    public void send() throws IOException {
        send((byte[]) null, null);
    }

    public void send(byte[] body, String contentType) throws IOException {
        setDefaultHeader(body, contentType);
        response.commitMessage(body);
    }

    public void send(String body, String contentType) throws IOException {
        send(body, StandardCharsets.UTF_8, contentType);
    }

    public void send(String body, Charset charset, String contentType) throws IOException {
        byte[] bodyBytes = getBodyString(body, charset);
        send(bodyBytes, contentType);
    }

    public void send(char[] body, String contentType) throws IOException {
        send(body, StandardCharsets.UTF_8, contentType);
    }

    public void send(char[] body, Charset charset, String contentType) throws IOException {
        byte[] bodyBytes = getCharArrayBytes(body, charset);
        send(bodyBytes, contentType);
    }

    public void sendError(HttpStatus status) throws IOException {
        sendError(status, null);
    }

    public void sendError(HttpStatus status, String errorMessage) throws IOException {
        this.setStatus(status);
        checkHttpStatusError(status);
        byte[] body = getBodyString(errorMessage, StandardCharsets.UTF_8);
        send(body, ResourceType.TEXT.getContentType("utf-8"));
    }

    private byte[] getBodyString(String body, Charset charset) {
        if (body == null) {
            return null;
        }
        return body.getBytes(charset);
    }

    private byte[] getCharArrayBytes(char[] body, Charset charset) throws CharacterCodingException {
        if (body == null) {
            return null;
        }
        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer buffer = encoder.encode(CharBuffer.wrap(body));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private static void checkHttpStatusError(HttpStatus status) {
        if (status.getCode() < 400) throw new IllegalArgumentException("HTTP status code is not error" + status.getCode());;
    }

    private void setDefaultHeader(byte[] body, String contentType) {
        HttpStatus status = response.getStatus();
        boolean isBodyNotAllowed = status.getCode() < 200 || status.getCode() == 204 || status.getCode() == 304;
        if (isBodyNotAllowed) return;
        if (isNotEmptyBody(body) && hasNotContentLength()) {
            setContentLength(body.length);
        }
        if (isNotEmptyContentType(contentType) && hasNotContentType()) {
            setContentType(contentType);
        }
    }

    private boolean hasNotContentType() {
        return response.getHeaders().getContentType() == null;
    }

    private boolean isNotEmptyContentType(String contentType) {
        return contentType != null && !contentType.isEmpty();
    }

    private boolean hasNotContentLength() {
        return response.getHeaders().getContentLength() <= 0;
    }

    private static boolean isNotEmptyBody(byte[] body) {
        return body != null && body.length > 0;
    }
}
