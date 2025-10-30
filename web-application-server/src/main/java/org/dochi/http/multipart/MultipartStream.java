package org.dochi.http.multipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MultipartStream {
    private static final Logger log = LoggerFactory.getLogger(MultipartStream.class);
    private static final int CR = '\r';  // Carriage Return
    private static final int LF = '\n';  // Line Feed
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
    private final InputStream in;

    public MultipartStream(InputStream in) {
        this.in = in;
    }

    public byte[] readCRLFLine(int maxSize) throws IOException {
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
                if (lineBuffer.size() >= maxSize) {
                    throw new IllegalArgumentException("max size exceeds the multipart limit bytes: " + maxSize);
                }
                lineBuffer.write(previousByte);
            }

            previousByte = currentByte;
        }
        return null;
    }
}