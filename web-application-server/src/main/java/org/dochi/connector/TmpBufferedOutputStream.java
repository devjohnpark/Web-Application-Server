package org.dochi.connector;

import org.dochi.webserver.socket.SocketWrapperBase;

import java.io.IOException;
import java.io.OutputStream;

public class TmpBufferedOutputStream extends OutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
    private SocketWrapperBase<?> socketWrapper;
    private final byte[] buffer;
    private int bufferPosition = 0;

    public TmpBufferedOutputStream() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public TmpBufferedOutputStream(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("buffer size need to be positive");
        }
        this.buffer = new byte[bufferSize];
    }

    public void init(SocketWrapperBase<?> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }

    @Override
    public void write(int b) throws IOException {
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
            socketWrapper.write(buffer, 0, bufferPosition);
            socketWrapper.flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장
            bufferPosition = 0;
        }
    }

    public void recycle() {
        bufferPosition = 0;
    }

    // 일반 출력 스트림 사용
    public TmpBufferedOutputStream getOutputStream() {
        return this;
    }
}