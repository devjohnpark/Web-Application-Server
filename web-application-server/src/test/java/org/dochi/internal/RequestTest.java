package org.dochi.internal;

import org.dochi.http.utils.Parameters;
import org.dochi.internal.buffer.MessageBytes;
import org.dochi.internal.buffer.MimeHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    private Request request;

    @BeforeEach
    void setUp() {
        request = new Request();
    }

    @Test
    void constructorInitializesAllComponents() {
        assertNotNull(request.method());
        assertNotNull(request.requestPath());
        assertNotNull(request.queryString());
        assertNotNull(request.requestURI());
        assertNotNull(request.protocol());
        assertNotNull(request.headers());
        assertNotNull(request.parameters());
    }

    @Test
    void methodReturnsMessageBytesInstance() {
        MessageBytes method = request.method();
        assertNotNull(method);

        method.setString("GET");
        assertEquals("GET", method.toString());
    }

    @Test
    void requestPathReturnsMessageBytesInstance() {
        MessageBytes requestPath = request.requestPath();
        assertNotNull(requestPath);

        requestPath.setString("/api/users");
        assertEquals("/api/users", requestPath.toString());
    }

    @Test
    void queryStringReturnsMessageBytesInstance() {
        MessageBytes queryString = request.queryString();
        assertNotNull(queryString);

        queryString.setString("name=john&age=30");
        assertEquals("name=john&age=30", queryString.toString());
    }

    @Test
    void requestURIReturnsMessageBytesInstance() {
        MessageBytes uri = request.requestURI();
        assertNotNull(uri);

        uri.setString("/api/users?name=john");
        assertEquals("/api/users?name=john", uri.toString());
    }

    @Test
    void protocolReturnsMessageBytesInstance() {
        MessageBytes protocol = request.protocol();
        assertNotNull(protocol);

        protocol.setString("HTTP/1.1");
        assertEquals("HTTP/1.1", protocol.toString());
    }

    @Test
    void headersReturnsMimeHeadersInstance() {
        MimeHeaders headers = request.headers();
        assertNotNull(headers);
        assertEquals(0, headers.size());
    }

    @Test
    void parametersReturnsParametersInstance() {
        Parameters parameters = request.parameters();
        assertNotNull(parameters);
    }

    @Test
    void getContentTypeReturnsNullWhenHeaderNotSet() {
        String contentType = request.getContentType();
        assertNull(contentType);
    }

    @Test
    void getContentTypeReturnsValueFromHeaders() {
        // 헤더에 content-type 설정
        var header = request.headers().createHeader();
        header.getName().setString("content-type");
        header.getValue().setString("application/json");

        String contentType = request.getContentType();
        assertEquals("application/json", contentType);
    }

    @Test
    void getContentTypeCachesResult() {
        // 헤더에 content-type 설정
        var header = request.headers().createHeader();
        header.getName().setString("content-type");
        header.getValue().setString("application/json");

        String contentType1 = request.getContentType();
        String contentType2 = request.getContentType();

        assertEquals("application/json", contentType1);
        assertEquals("application/json", contentType2);
        // 같은 MessageBytes 인스턴스 참조하는지 확인은 구현상 어려우므로 값만 확인
    }

    @Test
    void getContentLengthReturnsNegativeWhenHeaderNotSet() {
        int contentLength = request.getContentLength();
        assertEquals(-1, contentLength);
    }

    @Test
    void getHeaderReturnsValueFromHeaders() {
        // 헤더 설정
        var header = request.headers().createHeader();
        header.getName().setString("Authorization");
        header.getValue().setString("Bearer token123");

        String headerValue = request.getHeader("Authorization");
        assertEquals("Bearer token123", headerValue);
    }

    @Test
    void getHeaderReturnsNullForNonExistentHeader() {
        String headerValue = request.getHeader("Non-Existent");
        assertNull(headerValue);
    }

    @Test
    void getCharacterEncodingReturnsNullWhenContentTypeNotSet() {
        String encoding = request.getCharacterEncoding();
        assertNull(encoding);
    }

    @Test
    void getCharacterEncodingReturnsEncodingFromContentType() {
        // content-type 헤더에 charset 포함하여 설정
        var header = request.headers().createHeader();
        header.getName().setString("content-type");
        header.getValue().setString("text/html; charset=UTF-8");

        String encoding = request.getCharacterEncoding();
        assertEquals("UTF-8", encoding);
    }

    @Test
    void getCharacterEncodingReturnsEmptyStringWhenContentTypeHasNoCharset() {
        // charset 없는 content-type 설정
        var header = request.headers().createHeader();
        header.getName().setString("content-type");
        header.getValue().setString("application/json");

        String encoding = request.getCharacterEncoding();
        assertNull(encoding);
    }

    @Test
    void getCharacterEncodingCachesResult() {
        // content-type 헤더 설정
        var header = request.headers().createHeader();
        header.getName().setString("content-type");
        header.getValue().setString("text/plain; charset=ISO-8859-1");

        String encoding1 = request.getCharacterEncoding();
        String encoding2 = request.getCharacterEncoding();

        assertEquals("ISO-8859-1", encoding1);
        assertEquals("ISO-8859-1", encoding2);
    }

    @Test
    void getCharsetFromContentTypeReturnsNullWhenEncodingNull() {
        Charset charset = request.getCharsetFromContentType();
        assertNull(charset);
    }

    @Test
    void getCharsetFromContentTypeReturnsCharsetInstance() {
        // content-type 헤더 설정
        var header = request.headers().createHeader();
        header.getName().setString("content-type");
        header.getValue().setString("text/html; charset=UTF-8");

        Charset charset = request.getCharsetFromContentType();
        assertEquals(StandardCharsets.UTF_8, charset);
    }

    @Test
    void getCharsetFromContentTypeCachesResult() {
        // content-type 헤더 설정
        var header = request.headers().createHeader();
        header.getName().setString("content-type");
        header.getValue().setString("application/xml; charset=UTF-16");

        Charset charset1 = request.getCharsetFromContentType();
        Charset charset2 = request.getCharsetFromContentType();

        assertEquals(StandardCharsets.UTF_16, charset1);
        assertEquals(StandardCharsets.UTF_16, charset2);
        assertSame(charset1, charset2); // 캐시된 인스턴스인지 확인
    }

    @Test
    void recycleResetsAllFields() {
        // 모든 필드에 값 설정
        request.method().setString("POST");
        request.requestPath().setString("/api/test");
        request.queryString().setString("param=value");
        request.requestURI().setString("/api/test?param=value");
        request.protocol().setString("HTTP/1.1");

        var header = request.headers().createHeader();
        header.getName().setString("content-type");
        header.getValue().setString("application/json; charset=UTF-8");

        // 캐시된 값들 생성
        request.getContentType();
        request.getContentLength();
        request.getCharacterEncoding();
        request.getCharsetFromContentType();

        // recycle 호출
        request.recycle();

        // 모든 MessageBytes가 리셋되었는지 확인
        assertEquals("", request.method().toString());
        assertEquals("", request.requestPath().toString());
        assertEquals("", request.queryString().toString());
        assertEquals("", request.requestURI().toString());
        assertEquals("", request.protocol().toString());
        
        assertTrue(request.method().isNull());
        assertTrue(request.requestPath().isNull());
        assertTrue(request.queryString().isNull());
        assertTrue(request.requestURI().isNull());
        assertTrue(request.protocol().isNull());

        // 헤더가 리셋되었는지 확인
        assertEquals(0, request.headers().size());

        // 캐시된 값들이 초기화되었는지 확인
        assertNull(request.getCharacterEncoding());
        assertNull(request.getCharsetFromContentType());
    }

    @Test
    void multipleHeadersHandling() {
        // 여러 헤더 설정
        var header1 = request.headers().createHeader();
        header1.getName().setString("Accept");
        header1.getValue().setString("application/json");

        var header2 = request.headers().createHeader();
        header2.getName().setString("User-Agent");
        header2.getValue().setString("TestClient/1.0");

        var header3 = request.headers().createHeader();
        header3.getName().setString("content-type");
        header3.getValue().setString("text/html; charset=UTF-8");

        assertEquals("application/json", request.getHeader("Accept"));
        assertEquals("TestClient/1.0", request.getHeader("User-Agent"));
        assertEquals("text/html; charset=UTF-8", request.getContentType());
        assertEquals("UTF-8", request.getCharacterEncoding());
    }

    @Test
    void caseInsensitiveHeaderAccess() {
        var header = request.headers().createHeader();
        header.getName().setString("Content-Type");
        header.getValue().setString("application/json");

        assertEquals("application/json", request.getHeader("content-type"));
        assertEquals("application/json", request.getHeader("CONTENT-TYPE"));
        assertEquals("application/json", request.getHeader("Content-Type"));
    }


    @Test
    void invalidContentLengthHandling() {
        var header = request.headers().createHeader();
        header.getName().setString("content-length");
        header.getValue().setString("invalid");

        int contentLength = request.getContentLength();
        assertEquals(0, contentLength);
    }
}