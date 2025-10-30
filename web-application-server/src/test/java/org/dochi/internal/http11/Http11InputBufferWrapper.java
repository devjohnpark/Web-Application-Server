package org.dochi.internal.http11;

import org.dochi.internal.RequestMetadata;

import java.io.IOException;

public class Http11InputBufferWrapper {
    private final Http11InputBuffer inputBuffer;

    public Http11InputBufferWrapper(Http11InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public boolean parseHeader(RequestMetadata requestMetadata) throws IOException {
        return inputBuffer.parseHeader(requestMetadata);
    }
}
