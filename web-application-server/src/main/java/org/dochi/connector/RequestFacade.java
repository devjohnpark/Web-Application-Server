package org.dochi.connector;

import org.dochi.external.ExternalRequest;
import org.dochi.internal.RequestMetadata;
import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.multipart.Part;
import org.dochi.http.utils.HttpStatus;
import org.dochi.http.utils.MediaType;
import org.dochi.http.multipart.MultiPartParser;
import org.dochi.http.multipart.Multipart;
import org.dochi.http.multipart.MultipartStream;
import org.dochi.webserver.config.HttpReqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class RequestFacade implements InternalRequest, ExternalRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestFacade.class);

    private final RequestMetadata requestMetadata;
    private final InternalInputStream inputStream;
    private final InputBuffer inputBuffer;
    private final Multipart multipart;
    private final HttpReqConfig config;
    private boolean parametersParsed = false;
    private boolean multipartParsed = false;
    private boolean usingInputStream = false;

    public RequestFacade(HttpReqConfig httpReqConfig) {
        this.requestMetadata = new RequestMetadata();
        this.inputBuffer = new InputBuffer();
        this.inputStream = new InternalInputStream(this.inputBuffer);
        this.multipart = new Multipart();
        this.config = httpReqConfig;
    }

    // injection low level input buffer object (HTTP Version Coupling)
    @Override
    public void setInputBuffer(org.dochi.internal.buffer.InputBuffer inputBuffer) {
        if (inputBuffer == null) {
            throw new IllegalArgumentException("internal.InputBuffer is null");
        }
        this.inputBuffer.setInputBuffer(inputBuffer);
    }

    @Override
    public RequestMetadata getRequestMetadata() {
        return requestMetadata;
    }

    @Override
    public void recycle() {
        this.requestMetadata.recycle();
        this.inputBuffer.recycle();
        this.multipart.recycle();
        this.parametersParsed = false;
        this.multipartParsed = false;
        this.usingInputStream = false;
    }

    @Override
    public Part getPart(String partName) throws IOException, HttpStatusException {
        if (!this.parametersParsed) {
            parseParameters();
        }
        if (!this.multipartParsed) {
            MultiPartParser parser = new MultiPartParser(new MultipartStream(getInputStream()), config.getRequestHeaderMaxSize(), config.getRequestPayloadMaxSize());
            try {
                parser.parseParts(getParameter("boundary"), multipart);
            } catch (IllegalStateException e) {
                throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            multipartParsed = true;
        }
        return multipart.getPart(partName);
    }

    @Override
    public String getMethod() { return requestMetadata.method().toString(); }

    @Override
    public String getRequestURI() { return requestMetadata.requestURI().toString(); }

    @Override
    public String getPath() { return requestMetadata.requestPath().toString(); }

    @Override
    public String getQueryString() {
        return requestMetadata.queryString().toString();
    }

    @Override
    public String getProtocol() { return requestMetadata.protocol().toString(); }

    @Override
    public String getHeader(String key) {
        return requestMetadata.headers().getHeader(key);
    }

    @Override
    public String getContentType() {
        return requestMetadata.getContentType();
    }

    @Override
    public int getContentLength() {
        return requestMetadata.getContentLength();
    }

    @Override
    public String getCharacterEncoding() {
        return requestMetadata.getCharacterEncoding();
    }

    @Override
    public String getParameter(String key) throws IOException {
        if (!this.parametersParsed) {
            parseParameters();
        }
        return requestMetadata.parameters().getValue(key);
    }

    @Override
    public InputStream getInputStream() {
        if (usingInputStream) {
            throw new IllegalStateException("RequestFacade.getInputStream already used");
        }
        usingInputStream = true;
        return this.inputStream;
    }

    // WAS 기본 파라메터 파싱 (lazy loading and parsing)
    private void parseParameters() throws IOException {
        // 1. query string 파싱
        parseHeaderRequestParameters();
        // 2.content-type에 따라 파싱 (multipart/form-data와 application/x-www-form-urlencoded 기본 파싱)
        MediaType mediaType = MediaType.parseMediaType(this.getContentType()); // type/subtype 없으면 null 반환
        if ("application/x-www-form-urlencoded".equalsIgnoreCase(mediaType.getFullType())) {
            parseBodyRequestParameters(); // header와 body의 request parameter 중복시 body 값으로 덮어씌움
        } else if ("multipart/form-data".equalsIgnoreCase(mediaType.getFullType())) {
            // getPart() 메서드 주석에서 로직에서 확인
            requestMetadata.parameters().addParameter(mediaType.getParameterName(), mediaType.getParameterValue()); // boundary
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
        requestMetadata.parameters().addRequestParameters(new String(buf, requestMetadata.getCharsetFromContentType()));
    }

    private void parseHeaderRequestParameters() {
        if (!requestMetadata.queryString().isNull()) {
            requestMetadata.parameters().addRequestParameters(this.getQueryString());
        }
    }
}
