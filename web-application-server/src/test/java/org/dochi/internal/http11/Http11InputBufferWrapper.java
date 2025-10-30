package org.dochi.internal.http11;

import org.dochi.internal.RequestHeader;

import java.io.IOException;

public class Http11InputBufferWrapper {
    private final Http11InputBuffer inputBuffer;

    public Http11InputBufferWrapper(Http11InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public boolean parseHeader(RequestHeader requestHeader) throws IOException {
        return inputBuffer.parseHeader(requestHeader);
    }
}
