package org.dochi.http.request.multipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MultipartBodyUntilBoundary {
    private final ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
    private byte[] body;
    private byte[] boundary;

    public void setBoundary(byte[] boundary) {
        this.boundary = boundary;
    }

    public void setBody(byte[] body) throws IOException {
        bodyStream.write(body);
    }

    public byte[] getBody() {
        return bodyStream.toByteArray();
    }

    public byte[] getBoundary() {
        return boundary;
    }

    public void recycle() {
        if (bodyStream.size() != 0 || boundary != null) {
            bodyStream.reset();
            boundary = null;
        }
    }
}