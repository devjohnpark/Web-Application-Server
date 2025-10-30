package org.dochi.internal;

import org.dochi.http.utils.MediaType;
import org.dochi.http.utils.Parameters;
import org.dochi.internal.buffer.HeaderBytes;
import org.dochi.internal.buffer.Headers;

import java.nio.charset.Charset;

// 요청 메세지의 메타데이터를 파싱한것을 버퍼 구간으로 설정하고 디코딩한 메타데이터를 가져올수있는 객체
// 헤더 필드는 메모리 주소를 직접 참조해서 처음 조회 O(N) 이후에 다음번 조회시 O(1)
public final class RequestHeader {
    private final HeaderBytes method;
    private final HeaderBytes requestPath;
    private final HeaderBytes queryString;
    private final HeaderBytes uri;
    private final HeaderBytes protocol;
    private HeaderBytes contentLength;
    private HeaderBytes contentType;
    private final Headers headers;
    private String characterEncoding;
    private Charset charset;
    private final Parameters parameters;

    public RequestHeader() {
        this.requestPath = new HeaderBytes();
        this.queryString = new HeaderBytes();
        this.method = new HeaderBytes();
        this.uri = new HeaderBytes();
        this.protocol = new HeaderBytes();
        this.headers = new Headers();
        this.parameters = new Parameters();
    }

    public HeaderBytes method() { return this.method; }

    public HeaderBytes queryString() { return this.queryString; }

    public HeaderBytes requestPath() { return this.requestPath; }

    public HeaderBytes requestURI() { return this.uri; }

    public HeaderBytes protocol() { return this.protocol; }

    public Headers headers() { return this.headers; }

    public Parameters parameters() { return this.parameters; }

    public String getContentType() {
        if (this.contentType == null || contentType.isNull()) {
            this.contentType = this.headers.getValue("content-type");
        }
        return this.contentType != null ? this.contentType.toString() : null;
    }

    public int getContentLength() {
        if (this.contentLength == null || contentLength.isNull()) {
            this.contentLength = this.headers.getValue("content-length");
        }
        return this.contentLength != null ? this.contentLength.toInt() : -1;
    }

    public Charset getCharsetFromContentType() {
        if (this.charset == null) {
            this.getCharacterEncoding();
            if (this.characterEncoding != null) {
                this.charset = Charset.forName(this.characterEncoding);
            }
        }
        return this.charset;
    }

    public String getHeader(String name) {
        return this.headers.getHeader(name);
    }

    public String getCharacterEncoding() {
        if (this.characterEncoding == null || this.characterEncoding.isEmpty()) {
            this.characterEncoding = getCharsetEncodingFromContentType(this.getContentType());
        }
        return this.characterEncoding != null ? this.characterEncoding : null;
    }

    private String getCharsetEncodingFromContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        return MediaType.parseMediaType(contentType).getCharset();
    }

    public void recycle() {
        this.method.recycle();
        this.requestPath.recycle();
        this.queryString.recycle();
        this.uri.recycle();
        this.protocol.recycle();
        this.headers.recycle();
        this.parameters.recycle();
        this.contentLength = null;
        this.contentType = null;
        this.characterEncoding = null;
        this.charset = null;
    }
}
