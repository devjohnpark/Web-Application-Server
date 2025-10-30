package org.dochi.external;

import org.dochi.http.utils.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;

public interface ExternalResponse {
    ExternalResponse setStatus(HttpStatus status);
    ExternalResponse setHeader(String key, String value);
    ExternalResponse setCookie(String cookie);
    ExternalResponse setConnection(boolean isKeep);
    ExternalResponse setContentType(String contentType);
    ExternalResponse setContentLength(int contentLength);
    ExternalResponse setDateHeader(String date);
    ExternalResponse inActiveDateHeader();
    ExternalResponse activeDateHeader();

    void send(HttpStatus status) throws IOException;
    void send(HttpStatus status, byte[] body, String contentType) throws IOException;

    void sendError(HttpStatus status) throws IOException;
    void sendError(HttpStatus status, String errorMessage) throws IOException;

    OutputStream getOutputStream() throws IOException;
}
