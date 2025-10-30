package org.dochi.internal.http11;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.internal.RequestMetadata;
import org.dochi.webserver.socket.HttpClient;
import org.dochi.webserver.socket.BioSocketWrapperConnectionTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Http11InputBufferTest extends BioSocketWrapperConnectionTest {
    private static final Logger log = LoggerFactory.getLogger(Http11InputBufferTest.class);
    protected final int headerMaxSize = 1024;
    protected final RequestMetadata requestMetadata = new RequestMetadata();
    protected Http11InputBuffer inputBuffer = new Http11InputBuffer(headerMaxSize);
    protected HttpClient httpClient;

    @BeforeEach
    void init() {
        inputBuffer.init(serverConnectedSocket);
        httpClient = new HttpClient(clientConnectedSocket);
    }

    @AfterEach
    void destroy() {
        inputBuffer.recycle();
        requestMetadata.recycle();
    }

    @Test
    void valid_get() throws IOException {
        httpClient.doRequest("GET /user HTTP/1.1\r\nConnection: keep-alive\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(requestMetadata));
        assertEquals("GET", requestMetadata.method().toString());
        assertEquals("/user", requestMetadata.requestPath().toString());
        assertEquals("", requestMetadata.queryString().toString());
        assertEquals("HTTP/1.1", requestMetadata.protocol().toString());
        assertEquals("keep-alive", requestMetadata.headers().getHeader("Connection"));
    }

    @Test
    void valid_get2() throws IOException {
        String httpRequest = "GET / HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "Connection: keep-alive\r\n"
                + "Cache-Control: max-age=0\r\n"
                + "Upgrade-Insecure-Requests: 1\r\n"
                + "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36\r\n"
                + "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7\r\n"
                + "Accept-Encoding: gzip, deflate, br, zstd\r\n"
                + "Accept-Language: en-US,en;q=0.9,ko;q=0.8\r\n"
                + "Cookie: Idea-4a91a283=4d2152c0-f6eb-498f-a7ac-9ebbf2816f9c\r\n"
                + "Sec-Fetch-Dest: document\r\n"
                + "Sec-Fetch-Mode: navigate\r\n"
                + "Sec-Fetch-Site: none\r\n"
                + "Sec-Fetch-User: ?1\r\n"
                + "sec-ch-ua: \"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"\r\n"
                + "sec-ch-ua-mobile: ?0\r\n"
                + "sec-ch-ua-platform: \"macOS\"\r\n"
                + "\r\n";

        httpClient.doRequest(httpRequest.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(requestMetadata));
        assertEquals("GET", requestMetadata.method().toString());
        assertEquals("", requestMetadata.queryString().toString());
        assertEquals("HTTP/1.1", requestMetadata.protocol().toString());
        assertEquals("keep-alive", requestMetadata.headers().getHeader("Connection"));
        assertEquals("localhost:8080", requestMetadata.headers().getHeader("host"));
        assertEquals("keep-alive", requestMetadata.headers().getHeader("connection"));
        assertEquals("max-age=0", requestMetadata.headers().getHeader("cache-control"));
        assertEquals("1", requestMetadata.headers().getHeader("upgrade-insecure-requests"));
        assertTrue(requestMetadata.headers().getHeader("user-agent").contains("Mozilla/5.0"));
        assertTrue(requestMetadata.headers().getHeader("accept").contains("text/html"));
        assertEquals("gzip, deflate, br, zstd", requestMetadata.headers().getHeader("accept-encoding"));
        assertEquals("en-US,en;q=0.9,ko;q=0.8", requestMetadata.headers().getHeader("accept-language"));
        assertEquals("Idea-4a91a283=4d2152c0-f6eb-498f-a7ac-9ebbf2816f9c", requestMetadata.headers().getHeader("cookie"));
        assertEquals("document", requestMetadata.headers().getHeader("sec-fetch-dest"));
        assertEquals("navigate", requestMetadata.headers().getHeader("sec-fetch-mode"));
        assertEquals("none", requestMetadata.headers().getHeader("sec-fetch-site"));
        assertEquals("?1", requestMetadata.headers().getHeader("sec-fetch-user"));
        assertTrue(requestMetadata.headers().getHeader("sec-ch-ua").contains("Chromium"));
        assertEquals("?0", requestMetadata.headers().getHeader("sec-ch-ua-mobile"));
        assertEquals("\"macOS\"", requestMetadata.headers().getHeader("sec-ch-ua-platform"));
    }

    @Test
    void valid_get_querystring() throws IOException {
        httpClient.doRequest("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(requestMetadata));
        assertEquals("GET", requestMetadata.method().toString());
        assertEquals("/user?name=john%20park&password=1234", requestMetadata.requestURI().toString());
        assertEquals("/user", requestMetadata.requestPath().toString());
        assertEquals("name=john%20park&password=1234", requestMetadata.queryString().toString());
        assertEquals("HTTP/1.1", requestMetadata.protocol().toString());
        assertEquals("keep-alive", requestMetadata.headers().getHeader("Connection"));
    }

    @Test
    void valid_get_header_size_exceed() throws IOException {
        String body = "/user?name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        this.inputBuffer = new Http11InputBuffer(header.getBytes(StandardCharsets.ISO_8859_1).length - 1);
        inputBuffer.init(serverConnectedSocket);
        String message = header + body;

        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(HttpStatusException.class, () -> inputBuffer.parseHeader(requestMetadata));
    }

    @Test
    void valid_post_form_urlencoded() throws IOException {
        String body = "/user?name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        String message = header + body;

        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(requestMetadata));

        assertEquals("POST", requestMetadata.method().toString());
        assertEquals("/user", requestMetadata.requestURI().toString());
        assertEquals("HTTP/1.1", requestMetadata.protocol().toString());
        assertEquals("keep-alive", requestMetadata.headers().getHeader("Connection"));
        assertEquals("application/x-www-form-urlencoded; charset=utf-8", requestMetadata.getContentType());
        assertEquals("utf-8", requestMetadata.getCharacterEncoding());
        assertEquals("application/x-www-form-urlencoded; charset=utf-8", requestMetadata.getContentType());
        assertEquals(contentLength, requestMetadata.getContentLength());
    }

    @Test
    void invalid_request_line_only_method() throws IOException {
        String message = "GET \r\nHost: localhost\r\n\r\n";
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(HttpStatusException.class, () -> inputBuffer.parseHeader(requestMetadata));
    }

    @Test
    void invalid_request_line_non_protocol() throws IOException {
        String message = "GET /\r\nHost: localhost\r\n\r\n";
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(HttpStatusException.class, () -> inputBuffer.parseHeader(requestMetadata));
    }

    @Test
    void invalid_request_line_non_protocol3() throws IOException {
        String message = "met\r\nHost: localhost\r\n\r\n";
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(HttpStatusException.class, () -> inputBuffer.parseHeader(requestMetadata));
    }

    @Test
    void invalid_header_format_non_name() throws IOException {
        String message = "GET / HTTP/1.1\r\n: keep-alive\r\n\r\n";
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));

        assertThrows(HttpStatusException.class, () -> inputBuffer.parseHeader(requestMetadata));
    }

    @Test
    void invalid_header_format_non_value() throws IOException {
        httpClient.doRequest("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: \r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(HttpStatusException.class, () -> inputBuffer.parseHeader(requestMetadata));
    }
}



