package org.dochi.http.utils;

import java.util.HashMap;
import java.util.Map;

public class ResponseHeaders {
    private final Map<String, String> headers = new HashMap<>();
    public static final String SERVER = "Server";
    public static final String DATE = "Date";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONNECTION = "Connection";
    public static final String KEEP_ALIVE = "Keep-Alive";
    public static final String SET_COOKIE = "Set-Cookie";

    public void addHeader(String key, String value) {
        if (key == null || value == null || key.isEmpty()) {
            return;
        }
        headers.put(key, value);
    }

    public void addContentLength(int contentLength) {
        addHeader(CONTENT_LENGTH, String.valueOf(contentLength));
    }

    public int getContentLength() {
        String contentLength = headers.get(CONTENT_LENGTH);
        return contentLength != null ? Integer.parseInt(headers.get(CONTENT_LENGTH)) : 0;
    }

    public String getContentType() {
        return headers.get(CONTENT_TYPE);
    }

    public String getDate() {
        return headers.get(ResponseHeaders.DATE);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void clear() {
        if (headers.isEmpty()) {
            return;
        }
        headers.clear();
    }
}
