package org.dochi.connector;

import org.dochi.internal.http11.Http11InputBufferWrapper;
import org.dochi.webserver.connect.TestConnectorBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

class InternalInputStreamTest extends TestConnectorBase {
    private static final Logger log = LoggerFactory.getLogger(InternalInputStreamTest.class);
    private final InputBuffer inputBuffer = new InputBuffer();
    private final InternalInputStream internalInputStream = new InternalInputStream(this.inputBuffer);
    private final Http11InputBufferWrapper inputBufferWrapper = new Http11InputBufferWrapper(super.inputBuffer);

    @Override
    protected void setUpInternal() throws IOException {
        inputBuffer.setInputBuffer(super.inputBuffer);
    }

    @Override
    protected void tearDownInternal() throws IOException {
        inputBuffer.recycle();
    }

    @Test
    void read() throws IOException {
        String body = "name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnector: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
        String message = header + "\r\n" + body;
        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(this.inputBufferWrapper.parseHeader(request));
        assertEquals(internalInputStream.read(), 'n');
    }

    @Test
    void read_buffer() throws IOException {
        String body = "name=john%20park&password=1234";
        byte[] buf = new byte[1024];
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnector: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
        String message = header + "\r\n" + body;
        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(this.inputBufferWrapper.parseHeader(request));
        int n = internalInputStream.read(buf);
        assertArrayEquals(Arrays.copyOf(buf, n), body.getBytes(StandardCharsets.ISO_8859_1));
    }

    // blocking 발생
    @Test
    void read_buffer_length() throws IOException {
        String body = "name=john%20park&password=1234";
        byte[] buf = new byte[1024];
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnector: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
        String message = header + "\r\n" + body;
        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(this.inputBufferWrapper.parseHeader(request));
        int n = internalInputStream.read(buf, 0, 2);
        assertArrayEquals(Arrays.copyOf(buf, n), "na".getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    void read_from_input_stream_buffer() throws IOException {
        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
        int contentLength = buf.length;
        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnector: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);

        String message = header + "\r\n" + body;
        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(this.inputBufferWrapper.parseHeader(request));

        for (byte b : buf) {
            assertEquals(internalInputStream.read(), b);
        }
    }

    @Test
    void read_buffer_from_input_stream_buffer() throws IOException {
        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
        int contentLength = buf.length;
        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnector: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);

        String message = header + "\r\n" + body;
        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(this.inputBufferWrapper.parseHeader(request));

        byte[] bodyBuf = new byte[contentLength];
        int n = 0;
        while (n < contentLength) {
            n += internalInputStream.read(bodyBuf, n, contentLength - n);
        }
        assertArrayEquals(bodyBuf, body.getBytes(StandardCharsets.ISO_8859_1));
    }
}