package org.dochi.internal.parser;

import org.dochi.internal.http11.Http11InputBufferTest;
import org.dochi.internal.http11.Http11Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Http11ParserTest extends Http11InputBufferTest {

    private Http11Parser parser;

    @BeforeEach
    void setUp() {
        parser = new Http11Parser(inputBuffer, headerMaxSize);
    }

    @Test
    void parseRequestLine_get() throws IOException {
        String requestLine = "GET /path HTTP/1.1\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(requestHeader));
        assertEquals("GET", requestHeader.method().toString());
        assertEquals("/path", requestHeader.requestURI().toString());
        assertEquals("/path", requestHeader.requestPath().toString());
        assertEquals("HTTP/1.1", requestHeader.protocol().toString());
        assertTrue(requestHeader.queryString().isNull());
    }

    @Test
    void parseRequestLine_withQueryString() throws IOException {
        String requestLine = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(requestHeader));
        assertEquals("GET", requestHeader.method().toString());
        assertEquals("/user?name=john%20park&password=1234", requestHeader.requestURI().toString());
        assertEquals("/user", requestHeader.requestPath().toString());
        assertEquals("name=john%20park&password=1234", requestHeader.queryString().toString());
        assertEquals("HTTP/1.1", requestHeader.protocol().toString());
    }

    @Test
    void parseRequestLine_post() throws IOException {
        String requestLine = "POST /api/users HTTP/1.1\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(requestHeader));
        assertEquals("POST", requestHeader.method().toString());
        assertEquals("/api/users", requestHeader.requestURI().toString());
        assertEquals("/api/users", requestHeader.requestPath().toString());
        assertEquals("HTTP/1.1", requestHeader.protocol().toString());
    }

    @Test
    void parseRequestLine_incomplete() throws IOException {
        String requestLine = "GET /path";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(SocketTimeoutException.class, () -> parser.parseRequestLine(requestHeader));
    }

    @Test
    void parseRequestLine_invalidFormat() throws IOException {
        String requestLine = "GET\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));

        assertThrows(IllegalArgumentException.class, () -> parser.parseRequestLine(requestHeader));
    }

    @Test
    void parseHeaders_valid() throws IOException {
        String headers = "Host: localhost:8080\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 123\r\n" +
                "\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue( parser.parseHeaders(requestHeader));
        assertEquals(3, requestHeader.headers().size());
        assertEquals("localhost:8080", requestHeader.headers().getHeader("Host"));
        assertEquals("application/json", requestHeader.headers().getHeader("Content-Type"));
        assertEquals("123", requestHeader.headers().getHeader("Content-Length"));
    }

    @Test
    void parseHeaders_withSpaces() throws IOException {
        String headers = "Authorization: Bearer token123\r\n" +
                "User-Agent: Mozilla/5.0\r\n" +
                "\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseHeaders(requestHeader));
        assertEquals(2, requestHeader.headers().size());
        assertEquals("Bearer token123", requestHeader.headers().getHeader("Authorization"));
        assertEquals("Mozilla/5.0", requestHeader.headers().getHeader("User-Agent"));
    }

    @Test
    void parseHeaders_Empty() throws IOException {
        String headers = "\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertFalse(parser.parseHeaders(requestHeader));
        assertEquals(0, requestHeader.headers().size());
    }

    @Test
    void parseHeaders_invalidFormat() throws IOException {
        String headers = "InvalidHeader\r\n\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(IllegalArgumentException.class, () -> parser.parseHeaders(requestHeader));
    }

    @Test
    void completeHttpRequest() throws IOException {
        String header = "POST /api/users?active=true HTTP/1.1\r\n" +
                "Host: example.com\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 25\r\n" +
                "\r\n";
        httpClient.doRequest(header.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(requestHeader));
        assertTrue(parser.parseHeaders(requestHeader));
        assertEquals("POST", requestHeader.method().toString());
        assertEquals("/api/users?active=true", requestHeader.requestURI().toString());
        assertEquals("/api/users", requestHeader.requestPath().toString());
        assertEquals("active=true", requestHeader.queryString().toString());
        assertEquals("HTTP/1.1", requestHeader.protocol().toString());
        assertEquals(3, requestHeader.headers().size());
    }
}