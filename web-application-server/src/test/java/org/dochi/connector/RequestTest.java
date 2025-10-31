package org.dochi.connector;

import org.dochi.internal.http11.Http11InputBufferWrapper;
import org.dochi.webserver.connect.TestConnectionBase;
import org.dochi.webserver.attribute.HttpReqAttribute;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest extends TestConnectionBase {
    private final Request externalRequest = new Request(request, new HttpReqAttribute());
    private final Http11InputBufferWrapper inputBufferWrapper = new Http11InputBufferWrapper(inputBuffer);

    @Override
    protected void setUpInternal() throws IOException {
        externalRequest.setInputBuffer(inputBuffer);
    }

    @Override
    protected void tearDownInternal() throws IOException {
        externalRequest.recycle();
    }

    @Test
    void getParameter_queryString() throws IOException {
        String header = "GET /user?name=john%20park&age=20 HTTP/1.1\r\nConnection: keep-alive\r\n\r\n";
        client.doRequest(header.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBufferWrapper.parseHeader(request));
        assertThat(externalRequest.getMethod()).isEqualTo("GET");
        assertThat(externalRequest.getPath()).isEqualTo("/user");
        assertThat(externalRequest.getQueryString()).isEqualTo("name=john%20park&age=20");
        assertThat(externalRequest.getRequestURI()).isEqualTo("/user?name=john%20park&age=20");
        assertThat(externalRequest.getProtocol()).isEqualTo("HTTP/1.1");
        assertThat(externalRequest.getParameter("name")).isEqualTo("john park");
        assertThat(externalRequest.getParameter("age")).isEqualTo("20");
        assertThat(externalRequest.getHeader("Connection")).isEqualTo("keep-alive");
    }

    @Test
    void getParameter_formUrlEncoded() throws IOException {
        String body = "username=john+park&age=20";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length; // 30
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);

        String message = header + body;
        client.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBufferWrapper.parseHeader(request));
        assertThat(externalRequest.getContentLength()).isEqualTo(contentLength);
        assertThat(externalRequest.getContentType()).isEqualTo("application/x-www-form-urlencoded; charset=utf-8");
        assertThat(externalRequest.getMethod()).isEqualTo("POST");
        assertThat(externalRequest.getRequestURI()).isEqualTo("/user");
        assertThat(externalRequest.getPath()).isEqualTo("/user");
        assertThat(externalRequest.getProtocol()).isEqualTo("HTTP/1.1");
        assertThat(externalRequest.getCharacterEncoding()).isEqualTo("utf-8");
        assertThat(externalRequest.getParameter("username")).isEqualTo("john park");
        assertThat(externalRequest.getParameter("age")).isEqualTo("20");
        assertThat(externalRequest.getHeader("Connection")).isEqualTo("keep-alive");
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
        client.doRequest(message.getBytes(StandardCharsets.UTF_8));

        assertTrue(inputBufferWrapper.parseHeader(request));
        assertThat(externalRequest.getContentLength()).isEqualTo(contentLength);
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B", externalRequest.getContentType());
        assertThat(externalRequest.getParameter("boundary")).isEqualTo("----WebKitFormBoundarylwQGqAAJBIOZfE7B");
        assertThat(externalRequest.getHeader("Connection")).isEqualTo("keep-alive");
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
        client.doRequest(message.getBytes(StandardCharsets.UTF_8));

        assertTrue(inputBufferWrapper.parseHeader(request));
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B", externalRequest.getContentType());
        assertThat(externalRequest.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(externalRequest.getPart("username").getContent()).isEqualTo("john".getBytes(StandardCharsets.UTF_8));
        assertThat(externalRequest.getPart("age").getContent()).isEqualTo("4".getBytes(StandardCharsets.UTF_8));
        assertThat(externalRequest.getPart("file").getContent()).isEqualTo("21312445321553451234213412341234234124234".getBytes(StandardCharsets.UTF_8));
        assertNull(externalRequest.getCharacterEncoding());
    }

    @Test
    void getInputStream_read_byte() throws IOException {
        String body = "hello world";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);

        String message = header + body;
        client.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBufferWrapper.parseHeader(request));
        InputStream in = externalRequest.getInputStream();
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
        client.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBufferWrapper.parseHeader(request));
        InputStream in = externalRequest.getInputStream();
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
        client.doRequest(message.getBytes(StandardCharsets.UTF_8));
        assertTrue(inputBufferWrapper.parseHeader(request));
        InputStream in = externalRequest.getInputStream();
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
        assertThrows(IllegalArgumentException.class, () -> this.externalRequest.setInputBuffer(null));
    }

    @Test
    void header() {
        assertNotNull(request);
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
        client.doRequest(message.getBytes(StandardCharsets.UTF_8));

        assertTrue(inputBufferWrapper.parseHeader(request));
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B", externalRequest.getContentType());
        assertThat(externalRequest.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(externalRequest.getPart("username").getContent()).isEqualTo("john".getBytes(StandardCharsets.UTF_8));
        assertThat(externalRequest.getPart("age").getContent()).isEqualTo("4".getBytes(StandardCharsets.UTF_8));
        assertThat(externalRequest.getPart("file").getContent()).isEqualTo("21312445321553451234213412341234234124234".getBytes(StandardCharsets.UTF_8));
        assertNull(externalRequest.getCharacterEncoding());

        externalRequest.recycle();

        assertDoesNotThrow(externalRequest::getInputStream);
    }
}