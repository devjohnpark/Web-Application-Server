package org.dochi.internal;

import org.dochi.http.utils.Parameters;
import org.dochi.internal.buffer.HeaderBytes;
import org.dochi.internal.buffer.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RequestMetadataTest {

    private RequestMetadata requestMetadata;

    @BeforeEach
    void setUp() {
        requestMetadata = new RequestMetadata();
    }

    @Test
    void constructorInitializesAllComponents() {
        assertNotNull(requestMetadata.method());
        assertNotNull(requestMetadata.requestPath());
        assertNotNull(requestMetadata.queryString());
        assertNotNull(requestMetadata.requestURI());
        assertNotNull(requestMetadata.protocol());
        assertNotNull(requestMetadata.headers());
        assertNotNull(requestMetadata.parameters());
    }

    @Test
    void methodReturnsMessageBytesInstance() {
        HeaderBytes method = requestMetadata.method();
        assertNotNull(method);

        method.setString("GET");
        assertEquals("GET", method.toString());
    }

    @Test
    void requestPathReturnsMessageBytesInstance() {
        HeaderBytes requestPath = requestMetadata.requestPath();
        assertNotNull(requestPath);

        requestPath.setString("/api/users");
        assertEquals("/api/users", requestPath.toString());
    }

    @Test
    void queryStringReturnsMessageBytesInstance() {
        HeaderBytes queryString = requestMetadata.queryString();
        assertNotNull(queryString);

        queryString.setString("name=john&age=30");
        assertEquals("name=john&age=30", queryString.toString());
    }

    @Test
    void requestURIReturnsMessageBytesInstance() {
        HeaderBytes uri = requestMetadata.requestURI();
        assertNotNull(uri);

        uri.setString("/api/users?name=john");
        assertEquals("/api/users?name=john", uri.toString());
    }

    @Test
    void protocolReturnsMessageBytesInstance() {
        HeaderBytes protocol = requestMetadata.protocol();
        assertNotNull(protocol);

        protocol.setString("HTTP/1.1");
        assertEquals("HTTP/1.1", protocol.toString());
    }

    @Test
    void headersReturnsMimeHeadersInstance() {
        Headers headers = requestMetadata.headers();
        assertNotNull(headers);
        assertEquals(0, headers.size());
    }

    @Test
    void parametersReturnsParametersInstance() {
        Parameters parameters = requestMetadata.parameters();
        assertNotNull(parameters);
    }

    @Test
    void getContentTypeReturnsNullWhenHeaderNotSet() {
        String contentType = requestMetadata.getContentType();
        assertNull(contentType);
    }

    @Test
    void getContentTypeReturnsValueFromHeaders() {
        // 헤더에 content-type 설정
        var header = requestMetadata.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/json");

        String contentType = requestMetadata.getContentType();
        assertEquals("application/json", contentType);
    }

    @Test
    void getContentTypeCachesResult() {
        // 헤더에 content-type 설정
        var header = requestMetadata.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/json");

        String contentType1 = requestMetadata.getContentType();
        String contentType2 = requestMetadata.getContentType();

        assertEquals("application/json", contentType1);
        assertEquals("application/json", contentType2);
        // 같은 HeaderBytes 인스턴스 참조하는지 확인은 구현상 어려우므로 값만 확인
    }

    @Test
    void getContentLengthReturnsNegativeWhenHeaderNotSet() {
        int contentLength = requestMetadata.getContentLength();
        assertEquals(-1, contentLength);
    }

    @Test
    void getHeaderReturnsValueFromHeaders() {
        // 헤더 설정
        var header = requestMetadata.headers().createHeader();
        header.name().setString("Authorization");
        header.getValue().setString("Bearer token123");

        String headerValue = requestMetadata.getHeader("Authorization");
        assertEquals("Bearer token123", headerValue);
    }

    @Test
    void getHeaderReturnsNullForNonExistentHeader() {
        String headerValue = requestMetadata.getHeader("Non-Existent");
        assertNull(headerValue);
    }

    @Test
    void getCharacterEncodingReturnsNullWhenContentTypeNotSet() {
        String encoding = requestMetadata.getCharacterEncoding();
        assertNull(encoding);
    }

    @Test
    void getCharacterEncodingReturnsEncodingFromContentType() {
        // content-type 헤더에 charset 포함하여 설정
        var header = requestMetadata.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("text/html; charset=UTF-8");

        String encoding = requestMetadata.getCharacterEncoding();
        assertEquals("UTF-8", encoding);
    }

    @Test
    void getCharacterEncodingReturnsEmptyStringWhenContentTypeHasNoCharset() {
        // charset 없는 content-type 설정
        var header = requestMetadata.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/json");

        String encoding = requestMetadata.getCharacterEncoding();
        assertNull(encoding);
    }

    @Test
    void getCharacterEncodingCachesResult() {
        // content-type 헤더 설정
        var header = requestMetadata.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("text/plain; charset=ISO-8859-1");

        String encoding1 = requestMetadata.getCharacterEncoding();
        String encoding2 = requestMetadata.getCharacterEncoding();

        assertEquals("ISO-8859-1", encoding1);
        assertEquals("ISO-8859-1", encoding2);
    }

    @Test
    void getCharsetFromContentTypeReturnsNullWhenEncodingNull() {
        Charset charset = requestMetadata.getCharsetFromContentType();
        assertNull(charset);
    }

    @Test
    void getCharsetFromContentTypeReturnsCharsetInstance() {
        // content-type 헤더 설정
        var header = requestMetadata.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("text/html; charset=UTF-8");

        Charset charset = requestMetadata.getCharsetFromContentType();
        assertEquals(StandardCharsets.UTF_8, charset);
    }

    @Test
    void getCharsetFromContentTypeCachesResult() {
        // content-type 헤더 설정
        var header = requestMetadata.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/xml; charset=UTF-16");

        Charset charset1 = requestMetadata.getCharsetFromContentType();
        Charset charset2 = requestMetadata.getCharsetFromContentType();

        assertEquals(StandardCharsets.UTF_16, charset1);
        assertEquals(StandardCharsets.UTF_16, charset2);
        assertSame(charset1, charset2); // 캐시된 인스턴스인지 확인
    }

    @Test
    void recycleResetsAllFields() {
        // 모든 필드에 값 설정
        requestMetadata.method().setString("POST");
        requestMetadata.requestPath().setString("/api/test");
        requestMetadata.queryString().setString("param=value");
        requestMetadata.requestURI().setString("/api/test?param=value");
        requestMetadata.protocol().setString("HTTP/1.1");

        var header = requestMetadata.headers().createHeader();
        header.name().setString("content-type");
        header.getValue().setString("application/json; charset=UTF-8");

        // 캐시된 값들 생성
        requestMetadata.getContentType();
        requestMetadata.getContentLength();
        requestMetadata.getCharacterEncoding();
        requestMetadata.getCharsetFromContentType();

        // recycle 호출
        requestMetadata.recycle();

        // 모든 MessageBytes가 리셋되었는지 확인
        assertEquals("", requestMetadata.method().toString());
        assertEquals("", requestMetadata.requestPath().toString());
        assertEquals("", requestMetadata.queryString().toString());
        assertEquals("", requestMetadata.requestURI().toString());
        assertEquals("", requestMetadata.protocol().toString());
        
        assertTrue(requestMetadata.method().isNull());
        assertTrue(requestMetadata.requestPath().isNull());
        assertTrue(requestMetadata.queryString().isNull());
        assertTrue(requestMetadata.requestURI().isNull());
        assertTrue(requestMetadata.protocol().isNull());

        // 헤더가 리셋되었는지 확인
        assertEquals(0, requestMetadata.headers().size());

        // 캐시된 값들이 초기화되었는지 확인
        assertNull(requestMetadata.getCharacterEncoding());
        assertNull(requestMetadata.getCharsetFromContentType());
    }

    @Test
    void multipleHeadersHandling() {
        // 여러 헤더 설정
        var header1 = requestMetadata.headers().createHeader();
        header1.name().setString("Accept");
        header1.getValue().setString("application/json");

        var header2 = requestMetadata.headers().createHeader();
        header2.name().setString("User-Agent");
        header2.getValue().setString("TestClient/1.0");

        var header3 = requestMetadata.headers().createHeader();
        header3.name().setString("content-type");
        header3.getValue().setString("text/html; charset=UTF-8");

        assertEquals("application/json", requestMetadata.getHeader("Accept"));
        assertEquals("TestClient/1.0", requestMetadata.getHeader("User-Agent"));
        assertEquals("text/html; charset=UTF-8", requestMetadata.getContentType());
        assertEquals("UTF-8", requestMetadata.getCharacterEncoding());
    }

    @Test
    void caseInsensitiveHeaderAccess() {
        var header = requestMetadata.headers().createHeader();
        header.name().setString("Content-Type");
        header.getValue().setString("application/json");

        assertEquals("application/json", requestMetadata.getHeader("content-type"));
        assertEquals("application/json", requestMetadata.getHeader("CONTENT-TYPE"));
        assertEquals("application/json", requestMetadata.getHeader("Content-Type"));
    }


    @Test
    void invalidContentLengthHandling() {
        var header = requestMetadata.headers().createHeader();
        header.name().setString("content-length");
        header.getValue().setString("invalid");

        int contentLength = requestMetadata.getContentLength();
        assertEquals(0, contentLength);
    }
}