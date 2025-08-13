//package org.dochi.http.request;
//
//import org.junit.jupiter.api.Test;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//class HttpRequestTest {
//    private Request httpRequest;
//
//    private void createHttpRequest(String fileName) throws IOException {
//        String testDir = "./src/test/resources/";
//        BufferedSocketInputStream in = new FileInputStream(new File(testDir + fileName));
//        httpRequest = new Request(in);
//        httpRequest.isPrepareRequest();
//    }
//
//    @Test
//    void invalid_requestLine() throws IOException {
//        assertThrows(IllegalArgumentException.class, () -> createHttpRequest("http_req_invalid_request_line1.txt"));
//        assertThrows(IllegalArgumentException.class, () -> createHttpRequest("http_req_invalid_request_line2.txt"));
//    }
//
//    @Test
//    void get_path_queryString() throws IOException {
//        // given, when
////        createHttpRequest("http_req_get_query-string.txt");
//        String message = """
//                GET /user?name=john%20park&password=1234 HTTP/1.1\r
//                Connection: keep-alive\r
//                \r
//                """;
//
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
//        Request request = new Request(byteArrayInputStream);
//        request.isPrepareRequest();
//
//
//        // then
//        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET);
//        assertThat(request.getPath()).isEqualTo("/user");
//        assertThat(request.getRequestParameter("name")).isEqualTo("john park");
//        assertThat(request.getRequestParameter("password")).isEqualTo("1234");
//        assertThat(request.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
//        assertThat(request.getConnection()).isEqualTo("keep-alive");
//    }
//
//    @Test
//    void post() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post.txt");
//
//        // then
//        assertThat(httpRequest.getMethod()).isEqualTo(HttpMethod.POST);
//        assertThat(httpRequest.getPath()).isEqualTo("/user/create");
//        assertThat(httpRequest.getRequestParameter("userId")).isEqualTo("john park");
//        assertThat(httpRequest.getRequestParameter("password")).isEqualTo("1234");
//        assertThat(httpRequest.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
//        assertThat(httpRequest.getConnection()).isEqualTo("keep-alive");
//    }
//
//    @Test
//    void post_non_contentLength() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_non_content-length.txt");
//
//        // then
//        assertThat(httpRequest.getRequestParameter("userId")).isNull();
//        assertThat(httpRequest.getRequestParameter("password")).isNull();
//    }
//
//    @Test
//    void post_negative_contentLength() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_negative_content-length.txt");
//
//        // then
//        assertEquals(0, httpRequest.getContentLength());
//        assertThat(httpRequest.getRequestParameter("userId")).isNull();
//        assertThat(httpRequest.getRequestParameter("password")).isNull();
//    }
//
//    @Test
//    void post_non_contentType() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_non_content-type.txt");
//
//        // then
//        assertThat(httpRequest.getRequestParameter("userId")).isNull();
//        assertThat(httpRequest.getRequestParameter("password")).isNull();
//    }
//
//    @Test
//    void post_requestLine_body_params_duplication() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_request-params_duplication.txt");
//
//        // then
//        assertThat(httpRequest.getMethod()).isEqualTo(HttpMethod.POST);
//        assertThat(httpRequest.getPath()).isEqualTo("/user/create");
//        assertThat(httpRequest.getRequestParameter("userId")).isEqualTo("john park");
//        assertThat(httpRequest.getRequestParameter("password")).isEqualTo("1234");
//        assertThat(httpRequest.getRequestParameter("num")).isEqualTo("123445");
//        assertThat(httpRequest.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
//        assertThat(httpRequest.getConnection()).isEqualTo("keep-alive");
//    }
//
//    @Test
//    void post_getBodyAsString() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_content-type_text.txt");
//
//        // then
//        assertThat(httpRequest.getBodyAsString()).isEqualTo("hello world");
//    }
//}