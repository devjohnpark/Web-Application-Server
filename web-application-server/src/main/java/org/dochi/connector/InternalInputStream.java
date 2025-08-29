package org.dochi.connector;

import java.io.IOException;
import java.io.InputStream;

public class InternalInputStream extends InputStream {
    private final InputBuffer inputBuffer;

    protected InternalInputStream(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    @Override
    public int read() throws IOException {
        return inputBuffer.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputBuffer.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputBuffer.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        inputBuffer.close();
    }
}
