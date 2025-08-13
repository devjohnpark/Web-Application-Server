package org.dochi.http.request.data;

import org.dochi.http.util.HttpParser.Pair;

import java.util.HashMap;
import java.util.Map;

import static org.dochi.http.util.HttpParser.parseRequestLine;
import static org.dochi.http.util.HttpParser.parseRequestUri;

public class RequestMetadata implements RequestMetadataGetter {
    private HttpMethod method;
    private HttpVersion version;
    private String requestURI;
    private String path;
    private String queryString;

    public void addRequestLine(String requestLine) {
        if (requestLine == null) {
            return;
        }
        String[] tokens = parseRequestLine(requestLine);
        addMethod(tokens[0]);
        addRequestURI(tokens[1]);
        addVersion(tokens[2]);
    }

    private void addMethod(String method) {
        this.method = HttpMethod.fromString(method);
    }

    private void addRequestURI(String requestURI) {
        this.requestURI = requestURI;
        Pair pair = parseRequestUri(requestURI);
        path = pair.key();
        queryString = pair.value();
    }

    private void addVersion(String version) {
        this.version = HttpVersion.fromString(version);
    }

    public HttpMethod getMethod() { return method; }

    public String getRequestURI() { return requestURI; }

    public String getPath() { return path; }

    public String getQueryString() { return queryString; }

    public HttpVersion getHttpVersion() { return version; }
}
