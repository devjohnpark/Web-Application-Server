package org.dochi.internal;

import org.dochi.http.utils.MediaType;
import org.dochi.http.utils.Parameters;
import org.dochi.internal.buffer.HeaderBytes;
import org.dochi.internal.buffer.MimeHeaders;

import java.nio.charset.Charset;

// 요청 메세지의 메타데이터를 파싱한것을 버퍼 구간으로 설정하고 디코딩한 메타데이터를 가져올수있는 객체
// 헤더 필드는 메모리 주소를 직접 참조해서 처음 조회 O(N) 이후에 다음번 조회시 O(1)
public final class RequestMetadata {
    private final HeaderBytes methodMB;
    private final HeaderBytes requestPathMB;
    private final HeaderBytes queryStringMB;
    private final HeaderBytes uriMB;
    private final HeaderBytes protocolMB;
    private HeaderBytes contentLengthMB;
    private HeaderBytes contentTypeMB;
    private final MimeHeaders headers;
    private String characterEncoding;
    private Charset charset;
    private final Parameters parameters;

    public RequestMetadata() {
        this.requestPathMB = HeaderBytes.newInstance();
        this.queryStringMB = HeaderBytes.newInstance();
        this.methodMB = HeaderBytes.newInstance();
        this.uriMB = HeaderBytes.newInstance();
        this.protocolMB = HeaderBytes.newInstance();
        this.headers = new MimeHeaders();
        this.parameters = new Parameters();
    }

    public HeaderBytes method() { return this.methodMB; }

    public HeaderBytes queryString() { return this.queryStringMB; }

    public HeaderBytes requestPath() { return this.requestPathMB; }

    public HeaderBytes requestURI() { return this.uriMB; }

    public HeaderBytes protocol() { return this.protocolMB; }

    public MimeHeaders headers() { return this.headers; }

    public Parameters parameters() { return this.parameters; }

    public String getContentType() {
        if (this.contentTypeMB == null || contentTypeMB.isNull()) {
            this.contentTypeMB = this.headers.getValue("content-type");
        }
        return this.contentTypeMB != null ? this.contentTypeMB.toString() : null;
    }

    public int getContentLength() {
        if (this.contentLengthMB == null || contentLengthMB.isNull()) {
            this.contentLengthMB = this.headers.getValue("content-length");
        }
        return this.contentLengthMB != null ? this.contentLengthMB.toInt() : -1;
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
        this.methodMB.recycle();
        this.requestPathMB.recycle();
        this.queryStringMB.recycle();
        this.uriMB.recycle();
        this.protocolMB.recycle();
        this.headers.recycle();
        this.parameters.recycle();
        this.contentLengthMB = null;
        this.contentTypeMB = null;
        this.characterEncoding = null;
        this.charset = null;
    }
}
