package org.dochi.http.response.stream;

import java.io.IOException;
import java.io.OutputStream;

public class BufferedSocketOutputStream extends OutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final OutputStream outputStream;
    private final byte[] buffer;
    private int bufferPosition = 0;
    private boolean isBufferEnabled = true;

    public BufferedSocketOutputStream(OutputStream outputStream) {
        this(outputStream, DEFAULT_BUFFER_SIZE);
    }

    public BufferedSocketOutputStream(OutputStream outputStream, int bufferSize) {
        if (outputStream == null) {
            throw new IllegalArgumentException("OutputStream is null");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("buffer size need to be positive");
        }
        this.outputStream = outputStream;
        this.buffer = new byte[bufferSize];
    }

    @Override
    public void write(int b) throws IOException {
        if (isBufferEnabled) {
            writeToBuffer(b);
        } else {
            outputStream.write(b);
        }
    }

//    private boolean shouldBufferEnabled() {
//        return isBufferEnabled;
//    }

    private void writeToBuffer(int b) throws IOException {
        if (bufferPosition >= buffer.length) {
            flushBuffer();
        }
        buffer[bufferPosition++] = (byte) b;
    }

    @Override
    public void flush() throws IOException {
        flushBuffer();
    }

    private void flushBuffer() throws IOException {
        if (bufferPosition > 0) {
            outputStream.write(buffer, 0, bufferPosition);
            outputStream.flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장
            bufferPosition = 0;
        }
    }

    public void recycle() {
        bufferPosition = 0;
        isBufferEnabled = true;
    }

    // 일반 출력 스트림 사용
    public BufferedSocketOutputStream getOutputStream() {
        isBufferEnabled = false;
        return this;
    }
}