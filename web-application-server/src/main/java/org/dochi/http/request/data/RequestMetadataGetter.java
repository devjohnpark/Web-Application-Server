package org.dochi.http.request.data;

public interface RequestMetadataGetter {
    HttpMethod getMethod();

    String getRequestURI();

    String getPath();

    String getQueryString();

    HttpVersion getHttpVersion();
}
