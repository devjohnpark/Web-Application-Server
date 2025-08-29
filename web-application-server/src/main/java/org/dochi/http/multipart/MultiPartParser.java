package org.dochi.http.multipart;

//import org.dochi.http.request.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MultiPartParser {
    private static final Logger log = LoggerFactory.getLogger(MultiPartParser.class);
    private final BoundaryValidator boundaryValidator;
    private final MultipartSection section;

    private final MultipartStream stream;
    private final int headerMaxSize;
    private final int bodyMaxSize;

    public MultiPartParser(MultipartStream stream, int headerMaxSize, int bodyMaxSize) {
        this.stream = stream;
        this.section = new MultipartSection();
        this.boundaryValidator = new BoundaryValidator();
        this.headerMaxSize = headerMaxSize;
        this.bodyMaxSize = bodyMaxSize;
    }

    // HttpProcessorAttribute -> boolean http11InputBuffer.isParseHeader() -> false -> 로깅 -> 응답 중단
    // HttpProcessorAttribute -> boolean http11InputBuffer.isParseHeader() -> void InternalAdapter.service() -> HttpExternalRequest -> throws exception -> HttpProcessorAttribute catch
    public void parseParts(String boundaryValue, Multipart multipart) throws IOException {
        boundaryValidator.validateBoundary(boundaryValue);
        byte[] currentBoundary = stream.readCRLFLine(bodyMaxSize);
        while (boundaryValidator.isBoundary(currentBoundary)) {
            if (!parseMultipartSection()) {
                throw new EOFException();
            }
            storePart(multipart);
            currentBoundary = section.getBoundary();
        }
        validateMultipartData(currentBoundary);
    }

    private void validateMultipartData(byte[] currentBoundary) {
        if (!boundaryValidator.isEndBoundary(currentBoundary)) {
            throw new IllegalStateException("End of boundary not found from multipart/form-data.");
        }
    }

    private boolean parseMultipartSection() throws IOException {
        this.section.recycle();
        boolean b1 = parseHeaders(section.headers);
        parseParameters(section.headers, section.parameters);
        boolean b2 = parseBodyUntilBoundary(section.bodyUntilBoundary);
        return b1 && b2;
    }

    private boolean parseHeaders(MultipartHeaders headers) throws IOException {
        byte[] bytes;
        try {
            while ((bytes = stream.readCRLFLine(headerMaxSize)) != null) {
                if (bytes.length == 0) {
                    return true;
                }
                headers.addHeader(new String(bytes, StandardCharsets.US_ASCII));
            }
            return false;
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Multipart headers " + e.getMessage());
        }
    }

    // HTTP/1.1 표준(RFC 7231 및 RFC 7578)에 따르면: multipart/form-data 요청에는 적어도 하나 이상의 파트가 포함되어야 하며, 각 파트에는 메타데이터 헤더(Content-Disposition)가 필요
    private void parseParameters(MultipartHeaders headers, MultipartParameters parameters) {
        String contentDisposition = headers.getContentDisposition();
        if (contentDisposition != null) {
            parameters.addContentDispositionParameters(contentDisposition);
            return;
        }
        throw new IllegalStateException("Content-Disposition header not include for part from multipart/form-data format");
    }

    private boolean parseBodyUntilBoundary(MultipartBodyUntilBoundary bodyUntilBoundary) throws IOException {
        byte[] bytes;
        try {
            while ((bytes = stream.readCRLFLine(bodyMaxSize)) != null) {
                if (boundaryValidator.isBoundary(bytes)|| boundaryValidator.isEndBoundary(bytes)) {
                    bodyUntilBoundary.setBoundary(bytes);
                    return true;
                }
                bodyUntilBoundary.setBody(bytes);
            }
            return false;
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Multipart body " + e.getMessage());
        }
    }

    private void storePart(Multipart multipart) throws IOException {
        String name = section.getParameters().getNameParamValue();
        if (name == null) {
            throw new IllegalStateException("Header has no name for part from multipart/form-data format.");
        }
        multipart.addPart(name, createPart(section.getHeaders(), section.getParameters(), section.getBody()));
    }

    private Part createPart(MultipartHeaders headers, MultipartParameters parameters, byte[] body) throws IOException {
        String fileName = parameters.getFileNameParamValue();
        if (fileName != null) {
            return new Part(body, headers.getContentType(), fileName, UUID.randomUUID());
        }
        return new Part(body, headers.getContentType());
    }

    private static class BoundaryValidator {
        private static final String BOUNDARY_PREFIX = "--";
        private static final String CRLF = "\r\n";
        private static final int MAX_LENGTH = 70; // Boundary 최대 길이

        private byte[] boundary = null;
        private byte[] endBoundary = null;

        public void validateBoundary(String boundaryValue) {
            if (isInvalid(boundaryValue)) {
                throw new IllegalStateException("Invalid multipart/form-data boundary value: " + boundaryValue);
            }
            this.boundary = (BOUNDARY_PREFIX + boundaryValue).getBytes(StandardCharsets.US_ASCII);
            this.endBoundary = (BOUNDARY_PREFIX + boundaryValue + BOUNDARY_PREFIX).getBytes(StandardCharsets.US_ASCII);
        }

        public boolean isBoundary(byte[] line) {
            return isMatchBoundary(line, boundary);
        }

        public boolean isEndBoundary(byte[] line) {
            return isMatchBoundary(line, endBoundary);
        }

        private boolean isMatchBoundary(byte[] line, byte[] boundary) {
            if (line == null || line.length != boundary.length) return false;
            for (int i = 0; i < line.length; i++) {
                if (line[i] != boundary[i]) {
                    return false;
                }
            }
            return true;
        }

        // RFC 2046
        private boolean isInvalid(String boundary) {
            if (boundary == null || boundary.isEmpty()) {
                return false;
            }

            if (isPrefixBoundaryValid(boundary)) {
                return false;
            }

            return boundary.length() <= MAX_LENGTH &&
                    !boundary.contains(CRLF);
        }

        private boolean isPrefixBoundaryValid(String boundary) {
            // --와 동일하거나, -- 다음 문자로 -가 아닌 문자가 오면 안됨
            if (boundary.startsWith(BOUNDARY_PREFIX) && boundary.length() > 2) {
                // -- + -
                return boundary.charAt(2) == BOUNDARY_PREFIX.charAt(0);
            }
            return !boundary.equals(BOUNDARY_PREFIX);
        }
    }

    private static class MultipartBodyUntilBoundary {
        private final ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
        private byte[] body;
        private byte[] boundary;

        public void setBoundary(byte[] boundary) {
            this.boundary = boundary;
        }

        public void setBody(byte[] body) throws IOException {
            bodyStream.write(body);
        }

        public byte[] getBody() {
            return bodyStream.toByteArray();
        }

        public byte[] getBoundary() {
            return boundary;
        }

        public void recycle() {
            if (bodyStream.size() != 0 || boundary != null) {
                bodyStream.reset();
                boundary = null;
            }
        }
    }

    private static class MultipartSection {
        private final MultipartHeaders headers = new MultipartHeaders();
        private final MultipartParameters parameters = new MultipartParameters();
        private final MultipartBodyUntilBoundary bodyUntilBoundary = new MultipartBodyUntilBoundary();

        public MultipartHeaders getHeaders() {
            return headers;
        }

        public MultipartParameters getParameters() {
            return parameters;
        }

        public byte[] getBody() {
            return bodyUntilBoundary.getBody();
        }

        public byte[] getBoundary() {
            return bodyUntilBoundary.getBoundary();
        }

        public void recycle() {
            headers.recycle();
            parameters.recycle();
            bodyUntilBoundary.recycle();
        }
    }
}
