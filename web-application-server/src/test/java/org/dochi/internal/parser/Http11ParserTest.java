package org.dochi.internal.parser;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.internal.http11.Http11InputBufferTest;
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
        parser = new Http11Parser(inputBuffer);
    }

    @Test
    void parseRequestLine_get() throws IOException {
        String requestLine = "GET /path HTTP/1.1\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(request));
        assertEquals("GET", request.method().toString());
        assertEquals("/path", request.requestURI().toString());
        assertEquals("/path", request.requestPath().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertTrue(request.queryString().isNull());
    }

    @Test
    void parseRequestLine_withQueryString() throws IOException {
        String requestLine = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(request));
        assertEquals("GET", request.method().toString());
        assertEquals("/user?name=john%20park&password=1234", request.requestURI().toString());
        assertEquals("/user", request.requestPath().toString());
        assertEquals("name=john%20park&password=1234", request.queryString().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
    }

    @Test
    void parseRequestLine_post() throws IOException {
        String requestLine = "POST /api/users HTTP/1.1\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(request));
        assertEquals("POST", request.method().toString());
        assertEquals("/api/users", request.requestURI().toString());
        assertEquals("/api/users", request.requestPath().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
    }

    @Test
    void parseRequestLine_incomplete() throws IOException {
        String requestLine = "GET /path";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(SocketTimeoutException.class, () -> parser.parseRequestLine(request));
    }

    @Test
    void parseRequestLine_invalidFormat() throws IOException {
        String requestLine = "GET\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));

        assertThrows(HttpStatusException.class, () -> parser.parseRequestLine(request));
    }

    @Test
    void parseHeaders_valid() throws IOException {
        String headers = "Host: localhost:8080\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 123\r\n" +
                "\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue( parser.parseHeaders(request));
        assertEquals(3, request.headers().size());
        assertEquals("localhost:8080", request.headers().getHeader("Host"));
        assertEquals("application/json", request.headers().getHeader("Content-Type"));
        assertEquals("123", request.headers().getHeader("Content-Length"));
    }

    @Test
    void parseHeaders_withSpaces() throws IOException {
        String headers = "Authorization: Bearer token123\r\n" +
                "User-Agent: Mozilla/5.0\r\n" +
                "\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseHeaders(request));
        assertEquals(2, request.headers().size());
        assertEquals("Bearer token123", request.headers().getHeader("Authorization"));
        assertEquals("Mozilla/5.0", request.headers().getHeader("User-Agent"));
    }

    @Test
    void parseHeaders_Empty() throws IOException {
        String headers = "\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertFalse( parser.parseHeaders(request));
        assertEquals(0, request.headers().size());
    }

    @Test
    void parseHeaders_invalidFormat() throws IOException {
        String headers = "InvalidHeader\r\n\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(HttpStatusException.class, () -> parser.parseHeaders(request));
    }

    @Test
    void completeHttpRequest() throws IOException {
        String header = "POST /api/users?active=true HTTP/1.1\r\n" +
                "Host: example.com\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 25\r\n" +
                "\r\n";
        httpClient.doRequest(header.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(request));
        assertTrue(parser.parseHeaders(request));
        assertEquals("POST", request.method().toString());
        assertEquals("/api/users?active=true", request.requestURI().toString());
        assertEquals("/api/users", request.requestPath().toString());
        assertEquals("active=true", request.queryString().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals(3, request.headers().size());
    }
}