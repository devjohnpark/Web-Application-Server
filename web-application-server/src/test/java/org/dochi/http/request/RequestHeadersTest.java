//package org.dochi.http.request;
//
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class RequestHeadersTest {
//
//    @Test
//    void add_get_headers() {
//        MimeHeaders headers = new MimeHeaders();
//        String keepAlive = "keep-alive";
//        String cookie = "fdafasdfasdfasfsadf";
//        String contentType = "text/html; charset=utf-8";
//        int contentLength = 10;
//
//        headers.addHeader(String.format("Connection: %s", keepAlive));
//        headers.addHeader(String.format("Cookie: %s", cookie));
//        headers.addHeader(String.format("Content-Type: %s", contentType));
//        headers.addHeader(String.format("Content-Length: %d", contentLength));
//
//        assertThat(headers.getConnection()).isEqualTo(keepAlive);
//        assertThat(headers.getContentType()).isEqualTo(contentType);
//        assertThat(headers.getContentLength()).isEqualTo(contentLength);
//    }
//
//    @Test
//    void add_get_headers_negative_content_length() {
//        MimeHeaders headers = new MimeHeaders();
//        String contentType = "text/html; charset=utf-8";
//        int contentLength = -1;
//
//        headers.addHeader(String.format("Content-Type: %s", contentType));
//        headers.addHeader(String.format("Content-Length: %d", contentLength));
//
//        assertThat(headers.getContentType()).isEqualTo(contentType);
//        assertThat(headers.getContentLength()).isEqualTo(0);
//    }
//}