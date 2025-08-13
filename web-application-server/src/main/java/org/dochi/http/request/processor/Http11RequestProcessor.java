package org.dochi.http.request.processor;

import org.dochi.http.exception.HttpStatusException;

import org.dochi.http.monitor.ContentLengthValidator;
import org.dochi.http.monitor.MessageSizeMonitor;
import org.dochi.http.request.multipart.Part;
import org.dochi.http.request.stream.Http11RequestStream;
import org.dochi.http.response.HttpStatus;
import org.dochi.webresource.ResourceType;
import org.dochi.webserver.config.HttpReqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Http11RequestProcessor extends AbstractHttpRequestProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11RequestProcessor.class);
    private final ContentLengthValidator contentLengthValidator;

    // BufferedReader readLine() -> lock과 같은 동기화 로직때문에 요청당 스레드를 할당하는 서버 모델에서는 속도가 느림 & CRLF 단위로만 못읽고 \n 단위로도 읽음[
    private final Http11RequestStream requestStream;

    public Http11RequestProcessor(InputStream in, HttpReqConfig httpReqConfig) {
        super(in, httpReqConfig);
        this.requestStream = new Http11RequestStream(in);
        this.contentLengthValidator = new ContentLengthValidator(sizeMonitor.getContentMonitor());
    }

    @Override
    public boolean isProcessHeader(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
        return processRequestLine(sizeMonitor) && processHeaders(sizeMonitor);
    }

    private boolean processRequestLine(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
        String requestLine = requestStream.readHeader(sizeMonitor);
        if (isEOFOnCloseWait(requestLine)) {
            return false;
        }
        try {
            request.metadata().addRequestLine(requestLine);
            return true;
        } catch (IllegalArgumentException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private boolean isEOFOnCloseWait(String requestLine) {
        return requestLine == null;
    }

    private boolean processHeaders(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
        String line;
        while ((line = requestStream.readHeader(sizeMonitor)) != null) {
            if (line.isEmpty()) {
                return true;
            }
            request.headers().addHeaderLine(line);
        }
        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected end of stream while reading HTTP header");
    }

    @Override
    public byte[] getAllPayload() throws IOException, HttpStatusException {
        return contentLengthValidator.validateContentOnRead(request.headers().getContentLength(), contentLength ->
            requestStream.readAllBody(contentLength, sizeMonitor.getBodyMonitor())
        );
    }

    // 지연 읽기가 아직 커널 메모리에 저장되기 때문에 애플리케이션 메모리 과부하를 줄일수 있다. -> parts가 비었으면 처음으로 파싱을 수행
    @Override
    public Part getPart(String partName) throws IOException, HttpStatusException {
        ResourceType resourceType = ResourceType.MULTIPART;
        String contentType = request.headers().getContentType();
        String boundary = resourceType.getContentTypeParamValue(contentType);
        if (shouldProcessMultipart(resourceType, contentType)) {
            contentLengthValidator.validateContentOnRead(request.headers().getContentLength(), contentLength -> {
                multipartProcessor.processParts(
                        requestStream,
                        boundary,
                        request
                    );
                    return null;
                }
            );
        }
        return request.multipart().getPart(partName);
    }

    private boolean shouldProcessMultipart(ResourceType resourceType, String contentType) {
        return !request.multipart().isLoad() && resourceType.isEqualMimeType(contentType);
    }
}
