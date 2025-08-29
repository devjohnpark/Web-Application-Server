package org.dochi.connector;

import org.dochi.internal.http11.Http11InputBufferTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class InputBufferTest extends Http11InputBufferTest {
    private static final Logger log = LoggerFactory.getLogger(InputBufferTest.class);
    InputBuffer inputBuffer = new InputBuffer();

    @BeforeEach
    void setUp() {
        this.inputBuffer.setInputBuffer(super.inputBuffer);
    }

    @AfterEach
    void tearDown() {
        this.inputBuffer.recycle();
    }

    @Test
    void read() throws IOException {
        String body = "name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
        String message = header + "\r\n" + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader(request));
        assertEquals(this.inputBuffer.read(), 'n');
    }

    @Test
    void read_buffer() throws IOException {
        String body = "name=john%20park&password=1234";
        byte[] buf = new byte[1024];
        int contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        int headerSize = header.getBytes(StandardCharsets.UTF_8).length;
        System.out.println("header size: " + headerSize); // 133
        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader(request));

        byte[] bodyBuf = new byte[body.length()];
        int j = 0;
        int total = 0;
        while (total < contentLength) {
            int n = this.inputBuffer.read(buf);
            for (int i = 0; i < n; i++) {
                bodyBuf[j++] = (byte) buf[i];
            }
            total += n;
        }
        assertArrayEquals(bodyBuf, body.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    void read_buffer_length() throws IOException {
        String body = "name=john%20park&password=1234";
        byte[] buf = new byte[1024];
        int contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader(request));
        int n = this.inputBuffer.read(buf, 0, 2);
        assertArrayEquals(Arrays.copyOf(buf, n), "na".getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    void read_from_input_stream_buffer() throws IOException {
        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
        int contentLength = buf.length;
        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader(request));
        for (byte b : buf) {
            assertEquals(this.inputBuffer.read(), b);
        }
    }

    @Test
    void read_buffer_from_input_stream_buffer() throws IOException {
        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
        int contentLength = buf.length;
        System.out.println("content size: " + contentLength);
        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        System.out.println("header size: " + header.getBytes(StandardCharsets.ISO_8859_1).length);

        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader(request));

        byte[] bodyBuf = new byte[contentLength];
        int n = 0;
        while (n < contentLength) {
            n += this.inputBuffer.read(bodyBuf, n, contentLength - n);
        }
        assertArrayEquals(bodyBuf, body.getBytes(StandardCharsets.ISO_8859_1));
    }
}