package org.dochi.internal.buffer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ByteChunk {
    private static final Charset DEFAULT_CHARSET;
    private Charset charset;
    private byte[] buffer;
    private int start = 0;
    private int end = 0;

    public ByteChunk() {
        charset = DEFAULT_CHARSET;
    }

    static {
        DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    }

    public void setBytes(byte[] b, int off, int len) {
        this.buffer = b;
        this.start = off;
        this.end = this.start + len;
    }

    public void setCharset(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset must not be null");
        }
        this.charset = charset;
    }

    public byte[] getBuffer() {
        return this.buffer;
    }

    public int getLength() {
        return this.end - this.start;
    }

    public void recycle() {
        this.start = 0;
        this.end = 0;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public String toString() {
        if (end - start > 0) {
            return new String(buffer, start, end - start, charset);
        }
        return "";
    }

    public int toInt() {
        int result = 0;
        for (int i = start; i < end; i++) {
            byte b = buffer[i];
            if (b < '0' || b > '9') {
                throw new NumberFormatException("Invalid digit at index " + i + ": " + (char) b);
            }
            result = result * 10 + (b - '0');
        }
        return result;
    }

    public boolean equalsIgnoreCase(String s) {
        int len = end - start;
        if (buffer == null || s == null || len != s.length()) {
            return false;
        }
        int off = start;
        for (int i = 0; i < len; i++) {
            int b = buffer[off++] & 0xFF;
            char c = s.charAt(i);

            // 아스키 알파벳일 때만 비트 연산 적용
            // A: 0100 0001
            // a: 0110 0001
            if (b >= 'A' && b <= 'Z') {
                b |= 0x20; // 대문자와 소문자의 차이 0x20 (32)
            }
            if (c >= 'A' && c <= 'Z') {
                c |= 0x20;
            }

            if (b != c) {
                return false;
            }
        }

        return true;
    }
}
