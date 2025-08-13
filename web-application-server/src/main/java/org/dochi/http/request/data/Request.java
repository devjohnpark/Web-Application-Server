package org.dochi.http.request.data;

import org.dochi.http.request.multipart.Multipart;

import java.io.IOException;

public class Request implements Cloneable {
    private final RequestMetadata metadata = new RequestMetadata();
    private final RequestHeaders headers = new RequestHeaders();
    private final RequestParameters parameters = new RequestParameters();
    private final Multipart multipart = new Multipart();

    public RequestMetadata metadata() {
        return metadata;
    }

    public RequestHeaders headers() {
        return headers;
    }

    public RequestParameters parameters() {
        return parameters;
    }

    public Multipart multipart() {
        return multipart;
    }

    public void clear() throws IOException {
        headers.clear();
        parameters.clear();
        multipart.recycle();
    }

    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request) super.clone();
    }
}
