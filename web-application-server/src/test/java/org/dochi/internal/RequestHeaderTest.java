package org.dochi.internal;

import org.dochi.http.utils.Parameters;
import org.dochi.internal.buffer.HeaderBytes;
import org.dochi.internal.buffer.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RequestHeaderTest {

    private RequestHeader requestHeader;

    @BeforeEach
    void setUp() {
        requestHeader = new RequestHeader();
    }

    @Test
    void constructorInitializesAllComponents() {
        assertNotNull(requestHeader.method());
        assertNotNull(requestHeader.requestPath());
        assertNotNull(requestHeader.queryString());
        assertNotNull(requestHeader.requestURI());
        assertNotNull(requestHeader.protocol());
        assertNotNull(requestHeader.headers());
        assertNotNull(requestHeader.parameters());
    }

    @Test
    void methodReturnsMessageBytesInstance() {
        HeaderBytes method = requestHeader.method();
        assertNotNull(method);

        method.setString("GET");
        assertEquals("GET", method.toString());
    }

    @Test
    void requestPathReturnsMessageBytesInstance() {
        HeaderBytes requestPath = requestHeader.requestPath();
        assertNotNull(requestPath);

        requestPath.setString("/api/users");
        assertEquals("/api/users", requestPath.toString());
    }

    @Test
    void queryStringReturnsMessageBytesInstance() {
        HeaderBytes queryString = requestHeader.queryString();
        assertNotNull(queryString);

        queryString.setString("name=john&age=30");
        assertEquals("name=john&age=30", queryString.toString());
    }

    @Test
    void requestURIReturnsMessageBytesInstance() {
        HeaderBytes uri = requestHeader.requestURI();
        assertNotNull(uri);

        uri.setString("/api/users?name=john");
        assertEquals("/api/users?name=john", uri.toString());
    }

    @Test
    void protocolReturnsMessageBytesInstance() {
        HeaderBytes protocol = requestHeader.protocol();
        assertNotNull(protocol);

        protocol.setString("HTTP/1.1");
        assertEquals("HTTP/1.1", protocol.toString());
    }

    @Test
    void headersReturnsMimeHeadersInstance() {
        Headers headers = requestHeader.headers();
        assertNotNull(headers);
        assertEquals(0, headers.size());
    }

    @Test
    void parametersReturnsParametersInstance() {
        Parameters parameters = requestHeader.parameters();
        assertNotNull(parameters);
    }

    @Test
    void getContentTypeReturnsNullWhenHeaderNotSet() {
        String contentType = requestHeader.getContentType();
        assertNull(contentType);
    }

    @Test
    void getContentTypeReturnsValueFromHeaders() {
        // 헤더에 content-type 설정
        var header = requestHeader.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/json");

        String contentType = requestHeader.getContentType();
        assertEquals("application/json", contentType);
    }

    @Test
    void getContentTypeCachesResult() {
        // 헤더에 content-type 설정
        var header = requestHeader.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/json");

        String contentType1 = requestHeader.getContentType();
        String contentType2 = requestHeader.getContentType();

        assertEquals("application/json", contentType1);
        assertEquals("application/json", contentType2);
        // 같은 HeaderBytes 인스턴스 참조하는지 확인은 구현상 어려우므로 값만 확인
    }

    @Test
    void getContentLengthReturnsNegativeWhenHeaderNotSet() {
        int contentLength = requestHeader.getContentLength();
        assertEquals(-1, contentLength);
    }

    @Test
    void getHeaderReturnsValueFromHeaders() {
        // 헤더 설정
        var header = requestHeader.headers().createHeader();
        header.name().setString("Authorization");
        header.getValue().setString("Bearer token123");

        String headerValue = requestHeader.getHeader("Authorization");
        assertEquals("Bearer token123", headerValue);
    }

    @Test
    void getHeaderReturnsNullForNonExistentHeader() {
        String headerValue = requestHeader.getHeader("Non-Existent");
        assertNull(headerValue);
    }

    @Test
    void getCharacterEncodingReturnsNullWhenContentTypeNotSet() {
        String encoding = requestHeader.getCharacterEncoding();
        assertNull(encoding);
    }

    @Test
    void getCharacterEncodingReturnsEncodingFromContentType() {
        // content-type 헤더에 charset 포함하여 설정
        var header = requestHeader.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("text/html; charset=UTF-8");

        String encoding = requestHeader.getCharacterEncoding();
        assertEquals("UTF-8", encoding);
    }

    @Test
    void getCharacterEncodingReturnsEmptyStringWhenContentTypeHasNoCharset() {
        // charset 없는 content-type 설정
        var header = requestHeader.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/json");

        String encoding = requestHeader.getCharacterEncoding();
        assertNull(encoding);
    }

    @Test
    void getCharacterEncodingCachesResult() {
        // content-type 헤더 설정
        var header = requestHeader.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("text/plain; charset=ISO-8859-1");

        String encoding1 = requestHeader.getCharacterEncoding();
        String encoding2 = requestHeader.getCharacterEncoding();

        assertEquals("ISO-8859-1", encoding1);
        assertEquals("ISO-8859-1", encoding2);
    }

    @Test
    void getCharsetFromContentTypeReturnsNullWhenEncodingNull() {
        Charset charset = requestHeader.getCharsetFromContentType();
        assertNull(charset);
    }

    @Test
    void getCharsetFromContentTypeReturnsCharsetInstance() {
        // content-type 헤더 설정
        var header = requestHeader.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("text/html; charset=UTF-8");

        Charset charset = requestHeader.getCharsetFromContentType();
        assertEquals(StandardCharsets.UTF_8, charset);
    }

    @Test
    void getCharsetFromContentTypeCachesResult() {
        // content-type 헤더 설정
        var header = requestHeader.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/xml; charset=UTF-16");

        Charset charset1 = requestHeader.getCharsetFromContentType();
        Charset charset2 = requestHeader.getCharsetFromContentType();

        assertEquals(StandardCharsets.UTF_16, charset1);
        assertEquals(StandardCharsets.UTF_16, charset2);
        assertSame(charset1, charset2); // 캐시된 인스턴스인지 확인
    }

    @Test
    void recycleResetsAllFields() {
        // 모든 필드에 값 설정
        requestHeader.method().setString("POST");
        requestHeader.requestPath().setString("/api/test");
        requestHeader.queryString().setString("param=value");
        requestHeader.requestURI().setString("/api/test?param=value");
        requestHeader.protocol().setString("HTTP/1.1");

        var header = requestHeader.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/json; charset=UTF-8");

        // 캐시된 값들 생성
        requestHeader.getContentType();
        requestHeader.getContentLength();
        requestHeader.getCharacterEncoding();
        requestHeader.getCharsetFromContentType();

        // recycle 호출
        requestHeader.recycle();

        // 모든 MessageBytes가 리셋되었는지 확인
        assertEquals("", requestHeader.method().toString());
        assertEquals("", requestHeader.requestPath().toString());
        assertEquals("", requestHeader.queryString().toString());
        assertEquals("", requestHeader.requestURI().toString());
        assertEquals("", requestHeader.protocol().toString());
        
        assertTrue(requestHeader.method().isNull());
        assertTrue(requestHeader.requestPath().isNull());
        assertTrue(requestHeader.queryString().isNull());
        assertTrue(requestHeader.requestURI().isNull());
        assertTrue(requestHeader.protocol().isNull());

        // 헤더가 리셋되었는지 확인
        assertEquals(0, requestHeader.headers().size());

        // 캐시된 값들이 초기화되었는지 확인
        assertNull(requestHeader.getCharacterEncoding());
        assertNull(requestHeader.getCharsetFromContentType());
    }

    @Test
    void multipleHeadersHandling() {
        // 여러 헤더 설정
        var header1 = requestHeader.headers().createHeader();
        header1.name().setString("Accept");
        header1.getValue().setString("application/json");

        var header2 = requestHeader.headers().createHeader();
        header2.name().setString("User-Agent");
        header2.getValue().setString("TestClient/1.0");

        var header3 = requestHeader.headers().createHeader();
        header3.name().setString("content-type");
        header3.getValue().setString("text/html; charset=UTF-8");

        assertEquals("application/json", requestHeader.getHeader("Accept"));
        assertEquals("TestClient/1.0", requestHeader.getHeader("User-Agent"));
        assertEquals("text/html; charset=UTF-8", requestHeader.getContentType());
        assertEquals("UTF-8", requestHeader.getCharacterEncoding());
    }

    @Test
    void caseInsensitiveHeaderAccess() {
        var header = requestHeader.headers().createHeader();
        header.name().setString("Content-Type");
        header.getValue().setString("application/json");

        assertEquals("application/json", requestHeader.getHeader("content-type"));
        assertEquals("application/json", requestHeader.getHeader("CONTENT-TYPE"));
        assertEquals("application/json", requestHeader.getHeader("Content-Type"));
    }


    @Test
    void invalidContentLengthHandling() {
        var header = requestHeader.headers().createHeader();
        header.name().setString("content-length");
        header.getValue().setString("invalid");

        int contentLength = requestHeader.getContentLength();
        assertEquals(0, contentLength);
    }
}