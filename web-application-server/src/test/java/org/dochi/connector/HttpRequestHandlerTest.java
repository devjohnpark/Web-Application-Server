package org.dochi.connector;

import org.dochi.internal.http11.Http11InputBufferTest;
import org.dochi.webserver.attribute.HttpReqAttribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestHandlerTest extends Http11InputBufferTest {

    private final int headerMaxSize = 1024;
    private final HttpRequestHandler requestHandler = new HttpRequestHandler(new HttpReqAttribute());

    @BeforeEach
    void setUp() {
        this.requestHandler.setInputBuffer(inputBuffer);
    }

    @AfterEach
    void tearDown() {
        this.requestHandler.recycle();
    }

    @Test
    void getParameter_queryString() throws IOException {
        String header = "GET /user?name=john%20park&age=20 HTTP/1.1\r\nConnection: keep-alive\r\n\r\n";
        httpClient.doRequest(header.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBuffer.parseHeader(requestHandler.getRequest()));
        assertThat(requestHandler.getMethod()).isEqualTo("GET");
        assertThat(requestHandler.getPath()).isEqualTo("/user");
        assertThat(requestHandler.getQueryString()).isEqualTo("name=john%20park&age=20");
        assertThat(requestHandler.getRequestURI()).isEqualTo("/user?name=john%20park&age=20");
        assertThat(requestHandler.getProtocol()).isEqualTo("HTTP/1.1");
        assertThat(requestHandler.getParameter("name")).isEqualTo("john park");
        assertThat(requestHandler.getParameter("age")).isEqualTo("20");
        assertThat(requestHandler.getHeader("Connection")).isEqualTo("keep-alive");
    }

    @Test
    void getParameter_formUrlEncoded() throws IOException {
        String body = "username=john+park&age=20";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length; // 30
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);

        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBuffer.parseHeader(requestHandler.getRequest()));
        assertThat(requestHandler.getContentLength()).isEqualTo(contentLength);
        assertThat(requestHandler.getContentType()).isEqualTo("application/x-www-form-urlencoded; charset=utf-8");
        assertThat(requestHandler.getMethod()).isEqualTo("POST");
        assertThat(requestHandler.getRequestURI()).isEqualTo("/user");
        assertThat(requestHandler.getPath()).isEqualTo("/user");
        assertThat(requestHandler.getProtocol()).isEqualTo("HTTP/1.1");
        assertThat(requestHandler.getCharacterEncoding()).isEqualTo("utf-8");
        assertThat(requestHandler.getParameter("username")).isEqualTo("john park");
        assertThat(requestHandler.getParameter("age")).isEqualTo("20");
        assertThat(requestHandler.getHeader("Connection")).isEqualTo("keep-alive");
    }

    @Test
    void getParameter_multipartFormData_boundary() throws IOException {
        String body =
                "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                + "Content-Disposition: form-data; name=\"username\"\r\n"
                + "\r\n"
                + "john\r\n"

                + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                + "Content-Disposition: form-data; name=\"age\"\r\n"
                + "\r\n"
                + "4\r\n"

                + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"imageFile.png\"\r\n"
                + "Content-Type: image/png\r\n"
                + "\r\n"
                + "21312445321553451234213412341234234124234\r\n"
                + "------WebKitFormBoundarylwQGqAAJBIOZfE7B--\r\n";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);

        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));

        assertTrue(inputBuffer.parseHeader(requestHandler.getRequest()));
        assertThat(requestHandler.getContentLength()).isEqualTo(contentLength);
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B", requestHandler.getContentType());
        assertThat(requestHandler.getParameter("boundary")).isEqualTo("----WebKitFormBoundarylwQGqAAJBIOZfE7B");
        assertThat(requestHandler.getHeader("Connection")).isEqualTo("keep-alive");
    }

    @Test
    void getPart() throws IOException {
        String body =
                "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                + "Content-Disposition: form-data; name=\"username\"\r\n"
                + "\r\n"
                + "john\r\n"

                + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                + "Content-Disposition: form-data; name=\"age\"\r\n"
                + "\r\n"
                + "4\r\n"

                + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"imageFile.png\"\r\n"
                + "Content-Type: image/png\r\n"
                + "\r\n"
                + "21312445321553451234213412341234234124234\r\n"
                + "------WebKitFormBoundarylwQGqAAJBIOZfE7B--\r\n";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length; // 30
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);

        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));

        assertTrue(inputBuffer.parseHeader(requestHandler.getRequest()));
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B", requestHandler.getContentType());
        assertThat(requestHandler.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(requestHandler.getPart("username").getContent()).isEqualTo("john".getBytes(StandardCharsets.UTF_8));
        assertThat(requestHandler.getPart("age").getContent()).isEqualTo("4".getBytes(StandardCharsets.UTF_8));
        assertThat(requestHandler.getPart("file").getContent()).isEqualTo("21312445321553451234213412341234234124234".getBytes(StandardCharsets.UTF_8));
        assertNull(requestHandler.getCharacterEncoding());
    }

    @Test
    void getInputStream_read_byte() throws IOException {
        String body = "hello world";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);

        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBuffer.parseHeader(requestHandler.getRequest()));
        InputStream in = requestHandler.getInputStream();
        for (byte b: buf) {
            assertEquals(b, in.read());
        }
    }

    @Test
    void getInputStream_read_buffer_all() throws IOException {
        String body = "hello world";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBuffer.parseHeader(requestHandler.getRequest()));
        InputStream in = requestHandler.getInputStream();
        byte[] actualBuf = new byte[buf.length];
        in.read(actualBuf);
        assertArrayEquals(buf, actualBuf);
    }

    @Test
    void getInputStream_read_buffer_part() throws IOException {
        String body = "hello world";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length; // 30
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBuffer.parseHeader(requestHandler.getRequest()));
        InputStream in = requestHandler.getInputStream();
        byte[] actualBuf = new byte[buf.length];
        int off = 2;
        int len = buf.length - 2;
        in.read(actualBuf, off, len);
        for (int i = off; i < len; i++) {
            assertEquals(actualBuf[i], buf[i - off]);
        }
    }

    @Test
    void setInputBuffer() {
        assertThrows(IllegalArgumentException.class, () -> this.requestHandler.setInputBuffer(null));
    }

    @Test
    void getRequest() {
        assertNotNull(requestHandler.getRequest());
    }

    @Test
    void recycle() throws IOException {
        String body =
                "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                        + "Content-Disposition: form-data; name=\"username\"\r\n"
                        + "\r\n"
                        + "john\r\n"

                        + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                        + "Content-Disposition: form-data; name=\"age\"\r\n"
                        + "\r\n"
                        + "4\r\n"

                        + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                        + "Content-Disposition: form-data; name=\"file\"; filename=\"imageFile.png\"\r\n"
                        + "Content-Type: image/png\r\n"
                        + "\r\n"
                        + "21312445321553451234213412341234234124234\r\n"
                        + "------WebKitFormBoundarylwQGqAAJBIOZfE7B--\r\n";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length; // 30
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);

        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));

        assertTrue(inputBuffer.parseHeader(requestHandler.getRequest()));
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B", requestHandler.getContentType());
        assertThat(requestHandler.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(requestHandler.getPart("username").getContent()).isEqualTo("john".getBytes(StandardCharsets.UTF_8));
        assertThat(requestHandler.getPart("age").getContent()).isEqualTo("4".getBytes(StandardCharsets.UTF_8));
        assertThat(requestHandler.getPart("file").getContent()).isEqualTo("21312445321553451234213412341234234124234".getBytes(StandardCharsets.UTF_8));
        assertNull(requestHandler.getCharacterEncoding());

        requestHandler.recycle();

        assertDoesNotThrow(requestHandler::getInputStream);
        assertTrue(requestHandler.getRequest().method().isNull());
        assertNull(requestHandler.getRequest().parameters().getValue("boundary"));
    }
}