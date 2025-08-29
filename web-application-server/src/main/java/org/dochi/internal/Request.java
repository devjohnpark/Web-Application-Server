package org.dochi.internal;

import org.dochi.http.utils.MediaType;
import org.dochi.http.utils.Parameters;
import org.dochi.internal.buffer.ApplicationBufferHandler;
import org.dochi.internal.buffer.MessageBytes;
import org.dochi.internal.buffer.MimeHeaders;

import java.nio.charset.Charset;

// parsed raw data
public final class Request {
    private final MessageBytes methodMB;
    private final MessageBytes requestPathMB;
    private final MessageBytes queryStringMB;
    private final MessageBytes uriMB;
    private final MessageBytes protocolMB;
    private MessageBytes contentLengthMB;
    private MessageBytes contentTypeMB;
    private final MimeHeaders headers;
    private String characterEncoding;
    private Charset charset;
    private final Parameters parameters;
    private ApplicationBufferHandler handler;

    public Request() {
        this.requestPathMB = MessageBytes.newInstance();
        this.queryStringMB = MessageBytes.newInstance();
        this.methodMB = MessageBytes.newInstance();
        this.uriMB = MessageBytes.newInstance();
        this.protocolMB = MessageBytes.newInstance();
        this.headers = new MimeHeaders();
        this.parameters = new Parameters();
    }

    public MessageBytes method() { return this.methodMB; }

    public MessageBytes queryString() { return this.queryStringMB; }

    public MessageBytes requestPath() { return this.requestPathMB; }

    public MessageBytes requestURI() { return this.uriMB; }

    public MessageBytes protocol() { return this.protocolMB; }

    public MimeHeaders headers() { return this.headers; }

    public Parameters parameters() { return this.parameters; }

    // 헤더의 메모리 주소를 직접 참조하여, 처음 조회 O(N) 이후에 다음번 조회시 O(1)
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
