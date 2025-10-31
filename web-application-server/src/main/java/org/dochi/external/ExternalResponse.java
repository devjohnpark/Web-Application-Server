package org.dochi.external;

import org.dochi.http.utils.HttpStatus;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public interface ExternalResponse {
    ExternalResponse setStatus(HttpStatus status);
    ExternalResponse setHeader(String key, String value);
    ExternalResponse setCookie(String cookie);
    ExternalResponse setConnection(String connection);
    ExternalResponse setContentType(String contentType);
    ExternalResponse setContentLength(int contentLength);
    void send() throws IOException;
    void send(byte[] body, String contentType) throws IOException;
    void send(String body, String contentType) throws IOException;
    void send(String body, Charset charset, String contentType) throws IOException;
    void send(char[] body, String contentType) throws IOException;
    void send(char[] body, Charset charset, String contentType) throws IOException;
    void sendError(HttpStatus status) throws IOException;
    void sendError(HttpStatus status, String errorMessage) throws IOException;
    OutputStream getOutputStream() throws IOException;
}
