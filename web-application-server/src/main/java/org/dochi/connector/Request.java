package org.dochi.connector;

import org.dochi.external.ExternalRequest;
import org.dochi.http.multipart.MultiPartParser;
import org.dochi.http.multipart.Multipart;
import org.dochi.http.multipart.MultipartStream;
import org.dochi.http.multipart.Part;
import org.dochi.http.utils.MediaType;
import org.dochi.http.utils.Parameters;
import org.dochi.webserver.config.HttpReqConfig;

import java.io.IOException;
import java.io.InputStream;

public class Request extends RequestFacade implements ExternalRequest {
    protected final Multipart multipart;
    protected final Parameters parameters;
    protected InternalInputStream inputStream;
    protected boolean parametersParsed = false;
    protected boolean multipartParsed = false;
    private final HttpReqConfig config;

    public Request(org.dochi.internal.Request request, HttpReqConfig httpReqConfig) {
        super(request);
        this.multipart = new Multipart();
        this.parameters = new Parameters();
        this.config = httpReqConfig;
    }

    @Override
    public void recycle() {
        super.recycle();
        this.parameters.recycle();
        this.multipart.recycle();
        this.inputStream = null;
        this.parametersParsed = false;
        this.multipartParsed = false;
    }

    @Override
    public String getMethod() { return request.method().toString(); }

    @Override
    public String getRequestURI() { return request.requestURI().toString(); }

    @Override
    public String getPath() { return request.requestPath().toString(); }

    @Override
    public String getQueryString() {
        return request.queryString().toString();
    }

    @Override
    public String getProtocol() { return request.protocol().toString(); }

    @Override
    public String getHeader(String key) {
        return request.headers().getHeader(key);
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    @Override
    public String getParameter(String key) throws IOException {
        if (!this.parametersParsed) {
            parseParameters();
        }
        return parameters.getValue(key);
    }

    @Override
    public Part getPart(String partName) throws IOException {
        if (!this.parametersParsed) {
            parseParameters();
        }
        if (!this.multipartParsed) {
            MultiPartParser parser = new MultiPartParser(new MultipartStream(getInputStream()), config.getRequestHeaderMaxSize(), config.getRequestPayloadMaxSize());
            parser.parseParts(getParameter("boundary"), multipart);
            multipartParsed = true;
        }
        return multipart.getPart(partName);
    }

    @Override
    public InputStream getInputStream() {
        if (this.inputStream == null) {
            this.inputStream = new InternalInputStream(this.inputBuffer);
        }
        return this.inputStream;
    }

    // WebAppServer 기본 파라메터 파싱 (lazy loading and parsing)
    private void parseParameters() throws IOException {
        // 1. query string 파싱
        parseHeaderRequestParameters();
        // 2.content-type에 따라 파싱 (multipart/form-data와 application/x-www-form-urlencoded 기본 파싱)

        String media = this.getContentType();
        MediaType mediaType = MediaType.parseMediaType(this.getContentType()); // type/subtype 없으면 null 반환
        if ("application/x-www-form-urlencoded".equalsIgnoreCase(mediaType.getFullType())) {
            parseBodyRequestParameters(); // header와 body의 request parameter 중복시 body 값으로 덮어씌움
        } else if ("multipart/form-data".equalsIgnoreCase(mediaType.getFullType())) {
            // getPart() 메서드 주석에서 로직에서 확인
            parameters.addParameter(mediaType.getParameterName(), mediaType.getParameterValue()); // boundary
        }
        this.parametersParsed = true;
    }

    private void parseBodyRequestParameters() throws IOException {
        int contentLength = this.getContentLength();
        byte[] buf = new byte[contentLength];
        int n = 0;
        InputStream in = getInputStream();
        while (n < contentLength) {
            n += in.read(buf, n, contentLength - n);
        }
        parameters.addRequestParameters(new String(buf, request.getCharsetFromContentType()));
    }

    private void parseHeaderRequestParameters() {
        if (!request.queryString().isNull()) {
            parameters.addRequestParameters(this.getQueryString());
        }
    }
}
