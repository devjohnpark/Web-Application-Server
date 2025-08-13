//package org.dochi.http.request;
//
//import org.dochi.http.request.data.HttpMethod;
//import org.dochi.http.request.data.HttpVersion;
//import org.dochi.http.request.data.RequestLine;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class RequestLineTest {
//
//    @Test
//    void createRequestLine_only_path() {
//        RequestLine requestLine = RequestLine.createFromRequestLine("GET / HTTP/1.1");
//        assertThat(requestLine.getMethod()).isEqualTo(HttpMethod.GET);
//        assertThat(requestLine.getPath()).isEqualTo("/");
//        assertThat(requestLine.getVersion()).isEqualTo(HttpVersion.HTTP_1_1);
//    }
//
//    @Test
//    void createRequestLine_with_querystring() {
//        RequestLine requestLine = RequestLine.createFromRequestLine("GET /user?name=john%20park&age=20 HTTP/1.1");
//        assertThat(requestLine.getMethod()).isEqualTo(HttpMethod.GET);
//        assertThat(requestLine.getPath()).isEqualTo("/user");
//        assertThat(requestLine.getQueryString()).isEqualTo("name=john%20park&age=20");
//        assertThat(requestLine.getVersion()).isEqualTo(HttpVersion.HTTP_1_1);
//    }
//}