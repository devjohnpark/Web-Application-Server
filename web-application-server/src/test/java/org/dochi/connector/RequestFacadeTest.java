package org.dochi.connector;

import org.dochi.internal.http11.Http11InputBufferTest;
import org.dochi.internal.http11.Http11InputBufferWrapper;
import org.dochi.webserver.attribute.HttpReqAttribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RequestFacadeTest extends Http11InputBufferTest {

    private final int headerMaxSize = 1024;
    private final RequestFacade requestFacade = new RequestFacade(new HttpReqAttribute());
    Http11InputBufferWrapper inputBuffer = new Http11InputBufferWrapper(super.inputBuffer);

    @BeforeEach
    void setUp() {
        this.requestFacade.setInputBuffer(super.inputBuffer);
    }

    @AfterEach
    void tearDown() {
        this.requestFacade.recycle();
    }

    @Test
    void getParameter_queryString() throws IOException {
        String header = "GET /user?name=john%20park&age=20 HTTP/1.1\r\nConnection: keep-alive\r\n\r\n";
        httpClient.doRequest(header.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBuffer.parseHeader(requestFacade.getRequestHeader()));
        assertThat(requestFacade.getMethod()).isEqualTo("GET");
        assertThat(requestFacade.getPath()).isEqualTo("/user");
        assertThat(requestFacade.getQueryString()).isEqualTo("name=john%20park&age=20");
        assertThat(requestFacade.getRequestURI()).isEqualTo("/user?name=john%20park&age=20");
        assertThat(requestFacade.getProtocol()).isEqualTo("HTTP/1.1");
        assertThat(requestFacade.getParameter("name")).isEqualTo("john park");
        assertThat(requestFacade.getParameter("age")).isEqualTo("20");
        assertThat(requestFacade.getHeader("Connection")).isEqualTo("keep-alive");
    }

    @Test
    void getParameter_formUrlEncoded() throws IOException {
        String body = "username=john+park&age=20";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length; // 30
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);

        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBuffer.parseHeader(requestFacade.getRequestHeader()));
        assertThat(requestFacade.getContentLength()).isEqualTo(contentLength);
        assertThat(requestFacade.getContentType()).isEqualTo("application/x-www-form-urlencoded; charset=utf-8");
        assertThat(requestFacade.getMethod()).isEqualTo("POST");
        assertThat(requestFacade.getRequestURI()).isEqualTo("/user");
        assertThat(requestFacade.getPath()).isEqualTo("/user");
        assertThat(requestFacade.getProtocol()).isEqualTo("HTTP/1.1");
        assertThat(requestFacade.getCharacterEncoding()).isEqualTo("utf-8");
        assertThat(requestFacade.getParameter("username")).isEqualTo("john park");
        assertThat(requestFacade.getParameter("age")).isEqualTo("20");
        assertThat(requestFacade.getHeader("Connection")).isEqualTo("keep-alive");
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

        assertTrue(inputBuffer.parseHeader(requestFacade.getRequestHeader()));
        assertThat(requestFacade.getContentLength()).isEqualTo(contentLength);
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B", requestFacade.getContentType());
        assertThat(requestFacade.getParameter("boundary")).isEqualTo("----WebKitFormBoundarylwQGqAAJBIOZfE7B");
        assertThat(requestFacade.getHeader("Connection")).isEqualTo("keep-alive");
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

        assertTrue(inputBuffer.parseHeader(requestFacade.getRequestHeader()));
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B", requestFacade.getContentType());
        assertThat(requestFacade.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(requestFacade.getPart("username").getContent()).isEqualTo("john".getBytes(StandardCharsets.UTF_8));
        assertThat(requestFacade.getPart("age").getContent()).isEqualTo("4".getBytes(StandardCharsets.UTF_8));
        assertThat(requestFacade.getPart("file").getContent()).isEqualTo("21312445321553451234213412341234234124234".getBytes(StandardCharsets.UTF_8));
        assertNull(requestFacade.getCharacterEncoding());
    }

    @Test
    void getInputStream_read_byte() throws IOException {
        String body = "hello world";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);

        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBuffer.parseHeader(requestFacade.getRequestHeader()));
        InputStream in = requestFacade.getInputStream();
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
        assertTrue(inputBuffer.parseHeader(requestFacade.getRequestHeader()));
        InputStream in = requestFacade.getInputStream();
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
        assertTrue(inputBuffer.parseHeader(requestFacade.getRequestHeader()));
        InputStream in = requestFacade.getInputStream();
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
        assertThrows(IllegalArgumentException.class, () -> this.requestFacade.setInputBuffer(null));
    }

    @Test
    void getRequestHeader() {
        assertNotNull(requestFacade.getRequestHeader());
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

        assertTrue(inputBuffer.parseHeader(requestFacade.getRequestHeader()));
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B", requestFacade.getContentType());
        assertThat(requestFacade.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(requestFacade.getPart("username").getContent()).isEqualTo("john".getBytes(StandardCharsets.UTF_8));
        assertThat(requestFacade.getPart("age").getContent()).isEqualTo("4".getBytes(StandardCharsets.UTF_8));
        assertThat(requestFacade.getPart("file").getContent()).isEqualTo("21312445321553451234213412341234234124234".getBytes(StandardCharsets.UTF_8));
        assertNull(requestFacade.getCharacterEncoding());

        requestFacade.recycle();

        assertDoesNotThrow(requestFacade::getInputStream);
        assertTrue(requestFacade.getRequestHeader().method().isNull());
        assertNull(requestFacade.getRequestHeader().parameters().getValue("boundary"));
    }
}