package org.dochi.internal;

import org.dochi.http.utils.MediaType;
import org.dochi.internal.buffer.InputBuffer;
import java.nio.charset.Charset;

// 요청 메세지의 메타데이터를 파싱한것을 버퍼 구간으로 설정하고 디코딩한 메타데이터를 가져올수있는 객체
// 헤더 필드는 메모리 주소를 직접 참조해서 처음 조회 O(N) 이후에 다음번 조회시 O(1)
public final class Request implements RequestContext {
    private final HeaderBytes method;
    private final HeaderBytes requestPath;
    private final HeaderBytes queryString;
    private final HeaderBytes uri;
    private final HeaderBytes protocol;
    private HeaderBytes contentLength;
    private HeaderBytes contentType;
    private final Headers headers;
    private InputBuffer inputBuffer;
    private String characterEncoding;
    private Charset charset;

    public Request() {
        this.requestPath = new HeaderBytes();
        this.queryString = new HeaderBytes();
        this.method = new HeaderBytes();
        this.uri = new HeaderBytes();
        this.protocol = new HeaderBytes();
        this.headers = new Headers();
    }

    @Override
    public void setInputBuffer(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    @Override
    public void recycle() {
        this.method.recycle();
        this.requestPath.recycle();
        this.queryString.recycle();
        this.uri.recycle();
        this.protocol.recycle();
        this.headers.recycle();
        this.contentLength = null;
        this.contentType = null;
        this.characterEncoding = null;
        this.charset = null;
    }

    public HeaderBytes method() { return this.method; }

    public HeaderBytes queryString() { return this.queryString; }

    public HeaderBytes requestPath() { return this.requestPath; }

    public HeaderBytes requestURI() { return this.uri; }

    public HeaderBytes protocol() { return this.protocol; }

    public Headers headers() { return this.headers; }

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

    // Low-High Level을 잇는 역할의 Adapter는 각 서버 인스턴스 별로 하나 존재
    // internal.Request(Internal Request)과 connector.Request(External Request)는 1대1로 매칭되어야함
    // 방법 1. Adapter에서 External Request Pool 가지고 있음
    //  * 장점: 완전한 계층 분리
    //  * 단점: 싱글톤 객체인 InternalAdapter의 동시성 로직으로 인한 성능 저하
    // 방법 2. internal.Request에 connector.Request를 매핑
    //  * 장점: 간단하고 1:1 매핑 구조 적합
    //  * 단점: low-level 객체가 high level 객체를 역참조
    //    * internal.Request 가지고 외관(facade)으로 사용하는 단 하나는 객체 생성 허용
    //    * WAS에서 외부(개발자)로 노출되는 HTTP API 에 제공할 객체이므로 의미 부합

    // 결정: RequestContext 인터페이스로 상위 계층에서 자기 자신(internal.Request)를 맵핑시켜서 가져다 사용할수있게함
    // 효과: RequestContext 인터페이스로 각 계층 독립성 유지하면서 1:1 매핑 (완전한 계층 분리는 아니지만 성능을 위해 합리적인 결정)
    private RequestContext facade;

    public RequestContext getFacade() {
        return this.facade;
    }

    public void setFacade(RequestContext facade) {
        this.facade = facade;
        if (this.inputBuffer == null) {
            throw new IllegalStateException("Input buffer cannot be null");
        }
        if (this.facade == null) {
            throw new IllegalStateException("Facade cannot be null");
        }
        this.facade.setInputBuffer(this.inputBuffer);
    }
}
