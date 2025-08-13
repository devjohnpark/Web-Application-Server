package org.dochi.http.request.data;

public interface RequestHeadersGetter {
    String getHeader(String key);
    String getCookie();
    String getContentType();
    int getContentLength();
    String getConnection();
}
