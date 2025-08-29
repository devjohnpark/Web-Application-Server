package org.dochi.external;

import org.dochi.http.utils.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpExternalResponse {
    HttpExternalResponse addHeader(String key, String value);

    HttpExternalResponse addCookie(String cookie);

    HttpExternalResponse addConnection(boolean isKeep);

    HttpExternalResponse addDateHeaders(String date);

    HttpExternalResponse addContentHeaders(String contentType, int contentLength);

    HttpExternalResponse inActiveDateHeader();

    HttpExternalResponse activeDateHeader();

    void send(HttpStatus status) throws IOException;

    void send(HttpStatus status, byte[] body, String contentType) throws IOException;

    void sendError(HttpStatus status) throws IOException;

    void sendError(HttpStatus status, String errorMessage) throws IOException;

    OutputStream getOutputStream();
}
