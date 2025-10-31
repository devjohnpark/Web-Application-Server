package org.dochi.internal.http11;

import org.dochi.internal.request.Request;

import java.io.IOException;

public class Http11InputBufferWrapper {
    private final Http11InputBuffer inputBuffer;

    public Http11InputBufferWrapper(Http11InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public boolean parseHeader(Request request) throws IOException {
        return inputBuffer.parseHeader(request);
    }
}
