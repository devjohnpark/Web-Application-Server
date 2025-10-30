package org.dochi.external;

import org.dochi.http.utils.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;

public interface ExternalResponse {
    ExternalResponse addHeader(String key, String value);

    ExternalResponse addCookie(String cookie);

    ExternalResponse addConnection(boolean isKeep);

    ExternalResponse addDateHeaders(String date);

    ExternalResponse addContentHeaders(String contentType, int contentLength);

    ExternalResponse inActiveDateHeader();

    ExternalResponse activeDateHeader();

    void send(HttpStatus status) throws IOException;

    void send(HttpStatus status, byte[] body, String contentType) throws IOException;

    void sendError(HttpStatus status) throws IOException;

    void sendError(HttpStatus status, String errorMessage) throws IOException;

    OutputStream getOutputStream();
}
