package org.dochi.http.api;

import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.response.HttpStatus;
import org.dochi.http.response.ResponseHeaders;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpApiResponse {
    HttpApiResponse addHeader(String key, String value);

    HttpApiResponse addCookie(String cookie);

    HttpApiResponse addVersion(HttpVersion version);

    HttpApiResponse addConnection(boolean isKeepAlive);

    HttpApiResponse addKeepAlive(int timeout, int maxRequests);

    HttpApiResponse addStatus(HttpStatus status);

    void addDateHeaders(String date);

    void addContentHeaders(String contentType, int contentLength);

    void inActiveDateHeader();

    void activeDateHeader();

    void sendNoContent() throws IOException;

    void send(HttpStatus status) throws IOException;

    void send(HttpStatus status, byte[] body, String contentType) throws IOException;

    void sendError(HttpStatus status) throws IOException;

    void sendError(HttpStatus status, String errorMessage) throws IOException;

    OutputStream getOutputStream();

//    ResponseHeaders getHeaders();
}
