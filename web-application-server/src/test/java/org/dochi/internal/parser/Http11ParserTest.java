package org.dochi.internal.parser;

import org.dochi.http.exception.HttpStatusException;
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
        parser = new Http11Parser(inputBuffer);
    }

    @Test
    void parseRequestLine_get() throws IOException {
        String requestLine = "GET /path HTTP/1.1\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(requestMetadata));
        assertEquals("GET", requestMetadata.method().toString());
        assertEquals("/path", requestMetadata.requestURI().toString());
        assertEquals("/path", requestMetadata.requestPath().toString());
        assertEquals("HTTP/1.1", requestMetadata.protocol().toString());
        assertTrue(requestMetadata.queryString().isNull());
    }

    @Test
    void parseRequestLine_withQueryString() throws IOException {
        String requestLine = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(requestMetadata));
        assertEquals("GET", requestMetadata.method().toString());
        assertEquals("/user?name=john%20park&password=1234", requestMetadata.requestURI().toString());
        assertEquals("/user", requestMetadata.requestPath().toString());
        assertEquals("name=john%20park&password=1234", requestMetadata.queryString().toString());
        assertEquals("HTTP/1.1", requestMetadata.protocol().toString());
    }

    @Test
    void parseRequestLine_post() throws IOException {
        String requestLine = "POST /api/users HTTP/1.1\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(requestMetadata));
        assertEquals("POST", requestMetadata.method().toString());
        assertEquals("/api/users", requestMetadata.requestURI().toString());
        assertEquals("/api/users", requestMetadata.requestPath().toString());
        assertEquals("HTTP/1.1", requestMetadata.protocol().toString());
    }

    @Test
    void parseRequestLine_incomplete() throws IOException {
        String requestLine = "GET /path";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(SocketTimeoutException.class, () -> parser.parseRequestLine(requestMetadata));
    }

    @Test
    void parseRequestLine_invalidFormat() throws IOException {
        String requestLine = "GET\r\n";
        httpClient.doRequest(requestLine.getBytes(StandardCharsets.ISO_8859_1));

        assertThrows(HttpStatusException.class, () -> parser.parseRequestLine(requestMetadata));
    }

    @Test
    void parseHeaders_valid() throws IOException {
        String headers = "Host: localhost:8080\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 123\r\n" +
                "\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue( parser.parseHeaders(requestMetadata));
        assertEquals(3, requestMetadata.headers().size());
        assertEquals("localhost:8080", requestMetadata.headers().getHeader("Host"));
        assertEquals("application/json", requestMetadata.headers().getHeader("Content-Type"));
        assertEquals("123", requestMetadata.headers().getHeader("Content-Length"));
    }

    @Test
    void parseHeaders_withSpaces() throws IOException {
        String headers = "Authorization: Bearer token123\r\n" +
                "User-Agent: Mozilla/5.0\r\n" +
                "\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseHeaders(requestMetadata));
        assertEquals(2, requestMetadata.headers().size());
        assertEquals("Bearer token123", requestMetadata.headers().getHeader("Authorization"));
        assertEquals("Mozilla/5.0", requestMetadata.headers().getHeader("User-Agent"));
    }

    @Test
    void parseHeaders_Empty() throws IOException {
        String headers = "\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertFalse( parser.parseHeaders(requestMetadata));
        assertEquals(0, requestMetadata.headers().size());
    }

    @Test
    void parseHeaders_invalidFormat() throws IOException {
        String headers = "InvalidHeader\r\n\r\n";
        httpClient.doRequest(headers.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(HttpStatusException.class, () -> parser.parseHeaders(requestMetadata));
    }

    @Test
    void completeHttpRequest() throws IOException {
        String header = "POST /api/users?active=true HTTP/1.1\r\n" +
                "Host: example.com\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 25\r\n" +
                "\r\n";
        httpClient.doRequest(header.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(parser.parseRequestLine(requestMetadata));
        assertTrue(parser.parseHeaders(requestMetadata));
        assertEquals("POST", requestMetadata.method().toString());
        assertEquals("/api/users?active=true", requestMetadata.requestURI().toString());
        assertEquals("/api/users", requestMetadata.requestPath().toString());
        assertEquals("active=true", requestMetadata.queryString().toString());
        assertEquals("HTTP/1.1", requestMetadata.protocol().toString());
        assertEquals(3, requestMetadata.headers().size());
    }
}