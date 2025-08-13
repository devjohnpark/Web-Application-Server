package org.dochi.http.request.stream;

import java.io.IOException;

public class BufferedSocketInputStream extends java.io.InputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final java.io.InputStream inputStream;
    private final byte[] buffer;
    private int bufferPosition = 0;
    private int bufferSize = 0;
    private boolean isBufferEnabled = true;

    public BufferedSocketInputStream(java.io.InputStream inputStream) {
        this(inputStream, DEFAULT_BUFFER_SIZE);
    }

    public BufferedSocketInputStream(java.io.InputStream inputStream, int bufferSize) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream is null");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("buffer size need to be positive");
        }
        this.inputStream = inputStream;
        this.buffer = new byte[bufferSize];
    }

    @Override
    public int read() throws IOException {
        if (isBufferEnabled) {
            return readBuffer();
        }
        return inputStream.read();
    }

    private boolean shouldBufferEnabled() {
        return isBufferEnabled || bufferPosition < bufferSize;
    }

    private int readBuffer() throws IOException {
        if (bufferPosition >= bufferSize) {
            bufferSize = inputStream.read(buffer); // 0 ~ length
            bufferPosition = 0;
            if (bufferSize == -1) {
                return -1; // EOF
            }
        }
        return buffer[bufferPosition++] & 0xFF; // 1 byte 읽어서 int 형으로 표현
    }

//    @Override
//    public int read() throws IOException {
//        if (bufferPosition >= bufferSize) {
//            bufferSize = inputStream.read(buffer);
//            bufferPosition = 0;
//            if (bufferSize == -1) {
//                return -1; // EOF
//            }
//        }
//        return buffer[bufferPosition++] & 0xFF; // 1 byte 읽어서 int 형으로 표현
//    }

//    public int copyBuffer(byte[] buf) {
//        System.arraycopy(buffer, bufferPosition, buf, 0, bufferSize - bufferPosition);
//        return bufferSize - bufferPosition;
//    }
//
//    public int copyBuffer(char[] cb) {
//        char[] decodedChars = new String(buffer, 0, bufferSize - bufferPosition, StandardCharsets.UTF_8).toCharArray();
//        System.arraycopy(decodedChars, 0, cb, 0, decodedChars.length);
//        return decodedChars.length;
//    }

    public void recycle() {
        bufferPosition = 0;
        bufferSize = 0;
        isBufferEnabled = true;
    }

    // 일반 입력 스트림 사용
    // 입력 스트림으로 payload 읽기
    public BufferedSocketInputStream getInputStream() {
        isBufferEnabled = false;
        return this;
    }
}
