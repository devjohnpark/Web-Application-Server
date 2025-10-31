package org.dochi.internal.http11;

import org.dochi.internal.request.Request;
import org.dochi.webserver.connect.Client;
import org.dochi.webserver.connect.Connector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Http11InputBufferTest {
    private static final Logger log = LoggerFactory.getLogger(Http11InputBufferTest.class);
    private final Connector connector = new Connector();
    private final int headerMaxSize = 1024;
    private final Request request = new Request();
    private Http11InputBuffer inputBuffer = new Http11InputBuffer(headerMaxSize);
    private Client client;

    @BeforeEach
    void init() throws IOException {
        connector.connect();
        inputBuffer.init(connector.getServerConnectedSocket());
        client = new Client(connector.getClientConnectedSocket());
    }

    @AfterEach
    void destroy() throws IOException {
        inputBuffer.recycle();
        request.recycle();
        connector.disconnect();
    }

    @Test
    void valid_get() throws IOException {
        client.doRequest("GET /user HTTP/1.1\r\nConnector: keep-alive\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(request));
        assertEquals("GET", request.method().toString());
        assertEquals("/user", request.requestPath().toString());
        assertEquals("", request.queryString().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connector"));
    }

    @Test
    void valid_get2() throws IOException {
        String httpRequest = "GET / HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "Connector: keep-alive\r\n"
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

        client.doRequest(httpRequest.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(request));
        assertEquals("GET", request.method().toString());
        assertEquals("", request.queryString().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connector"));
        assertEquals("localhost:8080", request.headers().getHeader("host"));
        assertEquals("keep-alive", request.headers().getHeader("connector"));
        assertEquals("max-age=0", request.headers().getHeader("cache-control"));
        assertEquals("1", request.headers().getHeader("upgrade-insecure-requests"));
        assertTrue(request.headers().getHeader("user-agent").contains("Mozilla/5.0"));
        assertTrue(request.headers().getHeader("accept").contains("text/html"));
        assertEquals("gzip, deflate, br, zstd", request.headers().getHeader("accept-encoding"));
        assertEquals("en-US,en;q=0.9,ko;q=0.8", request.headers().getHeader("accept-language"));
        assertEquals("Idea-4a91a283=4d2152c0-f6eb-498f-a7ac-9ebbf2816f9c", request.headers().getHeader("cookie"));
        assertEquals("document", request.headers().getHeader("sec-fetch-dest"));
        assertEquals("navigate", request.headers().getHeader("sec-fetch-mode"));
        assertEquals("none", request.headers().getHeader("sec-fetch-site"));
        assertEquals("?1", request.headers().getHeader("sec-fetch-user"));
        assertTrue(request.headers().getHeader("sec-ch-ua").contains("Chromium"));
        assertEquals("?0", request.headers().getHeader("sec-ch-ua-mobile"));
        assertEquals("\"macOS\"", request.headers().getHeader("sec-ch-ua-platform"));
    }

    @Test
    void valid_get_querystring() throws IOException {
        client.doRequest("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnector: keep-alive\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(request));
        assertEquals("GET", request.method().toString());
        assertEquals("/user?name=john%20park&password=1234", request.requestURI().toString());
        assertEquals("/user", request.requestPath().toString());
        assertEquals("name=john%20park&password=1234", request.queryString().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connector"));
    }

    @Test
    void valid_get_header_size_exceed() throws IOException {
        String body = "/user?name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnector: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        this.inputBuffer = new Http11InputBuffer(buf.length - 1);
        inputBuffer.init(connector.getServerConnectedSocket());
        String message = header + body;
        client.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class, () -> inputBuffer.parseHeader(request));
    }

    @Test
    void valid_post_form_urlencoded() throws IOException {
        String body = "/user?name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnector: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        String message = header + body;

        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(request));

        assertEquals("POST", request.method().toString());
        assertEquals("/user", request.requestURI().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connector"));
        assertEquals("application/x-www-form-urlencoded; charset=utf-8", request.getContentType());
        assertEquals("utf-8", request.getCharacterEncoding());
        assertEquals("application/x-www-form-urlencoded; charset=utf-8", request.getContentType());
        assertEquals(contentLength, request.getContentLength());
    }

    @Test
    void invalid_request_line_only_method() throws IOException {
        String message = "GET \r\nHost: localhost\r\n\r\n";
        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(IllegalArgumentException.class, () -> inputBuffer.parseHeader(request));
    }

    @Test
    void invalid_request_line_non_protocol() throws IOException {
        String message = "GET /\r\nHost: localhost\r\n\r\n";
        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(IllegalArgumentException.class, () -> inputBuffer.parseHeader(request));
    }

    @Test
    void invalid_request_line_non_protocol3() throws IOException {
        String message = "met\r\nHost: localhost\r\n\r\n";
        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(IllegalArgumentException.class, () -> inputBuffer.parseHeader(request));
    }

    @Test
    void invalid_header_format_non_name() throws IOException {
        String message = "GET / HTTP/1.1\r\n: keep-alive\r\n\r\n";
        client.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));

        assertThrows(IllegalArgumentException.class, () -> inputBuffer.parseHeader(request));
    }

    @Test
    void invalid_header_format_non_value() throws IOException {
        client.doRequest("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnector: \r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(IllegalArgumentException.class, () -> inputBuffer.parseHeader(request));
    }
}



