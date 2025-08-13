package org.dochi.http.request.stream;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.monitor.MessageSizeMonitor;
import org.dochi.http.response.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Http11RequestStream implements HttpCrlfLineReader, HttpBodyReader {
    private static final Logger log = LoggerFactory.getLogger(Http11RequestStream.class);
    private static final int CR = '\r';  // Carriage Return
    private static final int LF = '\n';  // Line Feed
    private final InputStream in;
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

    public Http11RequestStream(InputStream in) {
        this.in = in;
    }

    public String readHeader(MessageSizeMonitor sizeMonitor) throws IOException {
        try {
            byte[] lineBytes = readLine(sizeMonitor.getSizeLimit());
            if (lineBytes != null) {
                sizeMonitor.monitorSize(lineBytes.length);
                return new String(lineBytes, StandardCharsets.UTF_8);
            }
        } catch (LineTooLongIOException e) {
            sizeMonitor.monitorSize(e.getLimitLineSize());
        }
        return null;
    }

    public byte[] readLineBytes(MessageSizeMonitor sizeMonitor) throws IOException {
        try {
            byte[] lineBytes = readLine(sizeMonitor.getSizeLimit());
            if (lineBytes != null) {
                sizeMonitor.monitorSize(lineBytes.length);
                return lineBytes;
            }
        } catch (LineTooLongIOException e) {
            sizeMonitor.monitorSize(e.getLimitLineSize());
        }
        return null;
    }

    public byte[] readAllBody(int contentLength, MessageSizeMonitor sizeMonitor) throws IOException {
        byte[] body = new byte[contentLength];
        int actualContentLength = in.read(body, 0, contentLength);
        if (actualContentLength == -1) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected end of stream while reading http body");
        }
        sizeMonitor.monitorSize(actualContentLength); // 라인 크기 전달
        return body;
    }

    // lineBuffer를 사용하면 또 데이터를 중복 저장해야 하므로 SocketBufferedInputStream에서 가져온다.
    private byte[] readLine(int limitLineSize) throws IOException {
        lineBuffer.reset();
        int previousByte = -1;
        int currentByte;

        while ((currentByte = in.read()) != -1) {

            // CR+LF 조합을 찾으면 줄의 끝
            if (previousByte == CR && currentByte == LF) {
                // 마지막 CRLF 문자 제거
                return lineBuffer.toByteArray();
            }

            if (previousByte != -1) {
                if (lineBuffer.size() >= limitLineSize) {
                    throw new LineTooLongIOException("Read line size exceeds the limit bytes", limitLineSize);
                }
                lineBuffer.write(previousByte);
            }

            previousByte = currentByte;
        }
        return null;

//        throw new NotFoundCrlfIOException("Unexpected end of stream for missing CRLF", lineBuffer.toString(StandardCharsets.UTF_8));
    }

    private static class LineTooLongIOException extends IOException {
        private final int limitLineSize;

        public LineTooLongIOException(String message, int limitLineSize) {
            super(message);
            this.limitLineSize = limitLineSize;
        }

        public int getLimitLineSize() {
            return limitLineSize;
        }

        @Override
        public String getMessage() {
            return super.getMessage() + ": " + limitLineSize;
        }
    }

//    private static class NotFoundCrlfIOException extends IOException {
//        private final String line;
//
//        public NotFoundCrlfIOException(String message, String line) {
//            super(message);
//            this.line = line;
//        }
//
//        public String getLine() {
//            return line;
//        }
//
//        @Override
//        public String getMessage() {
//            return super.getMessage() + ": " + line;
//        }
//    }
}