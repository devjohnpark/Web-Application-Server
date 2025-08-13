package org.dochi.http.request.multipart;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.monitor.MessageSizeMonitor;
import org.dochi.http.request.data.Request;
import org.dochi.http.request.stream.HttpCrlfLineReader;
import org.dochi.http.response.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

// file size는 일반 content가 아니고 용량이 크기 때문에 따로 제한하도록 해야함
public class MultiPartProcessor {
    private static final Logger log = LoggerFactory.getLogger(MultiPartProcessor.class);
//    private final Map<String, Part> parts = new HashMap<>();
    private final MessageSizeMonitor sizeMonitor;
    private final BoundaryValidator boundaryValidator = new BoundaryValidator();
    private final MultipartSection section;
//    private final MultipartHeaders headers = new MultipartHeaders();
//    private final MultipartParameters parameters = new MultipartParameters();
//    private final MultipartBodyUntilBoundary bodyUntilBoundary = new MultipartBodyUntilBoundary();
//    private final MultipartSection recycleSection = new MultipartSection(headers, parameters, bodyUntilBoundary);

//    public void processParts(HttpCrlfLineReader lineReader, String boundaryValue, MessageSizeMonitor sizeMonitor) throws HttpStatusException, IOException {
//        boundaryValidator.validateBoundary(boundaryValue);
//        byte[] currentBoundary = lineReader.readLineBytes(sizeMonitor);
//        while (boundaryValidator.isBoundary(currentBoundary)) {
//            processMultipartSection(lineReader, sizeMonitor);
//            storePart(section.getHeaders(), section.getParameters(), section.getBody());
//            currentBoundary = section.getBoundary();
//        }
//        validateMultipartData(currentBoundary);
//    }


    public MultiPartProcessor(MessageSizeMonitor sizeMonitor) {
        this.sizeMonitor = sizeMonitor;
        this.section = new MultipartSection();
    }

    public void processParts(HttpCrlfLineReader lineReader, String boundaryValue, Request request) throws IOException {
        boundaryValidator.validateBoundary(boundaryValue);
        byte[] currentBoundary = lineReader.readLineBytes(sizeMonitor);
        while (boundaryValidator.isBoundary(currentBoundary)) {
            MultipartSection section = processMultipartSection(lineReader);
            storePart(section.getHeaders(), section.getParameters(), section.getBody(), request);
            currentBoundary = section.getBoundary();
        }
        validateMultipartData(currentBoundary);
    }

    private void validateMultipartData(byte[] currentBoundary) {
        if (!boundaryValidator.isEndBoundary(currentBoundary)) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "End of boundary not found from multipart/form-data.");
        }
    }

    private MultipartSection processMultipartSection(HttpCrlfLineReader lineReader) throws IOException {
        MultipartSection section = this.section.recycle();
        processHeaders(section.headers, lineReader);
        processParameters(section.headers, section.parameters);
        processBodyUntilBoundary(section.bodyUntilBoundary, lineReader);
        return section;
    }

    private void processHeaders(MultipartHeaders headers, HttpCrlfLineReader lineReader) throws IOException {
        String line;
        while ((line = lineReader.readHeader(sizeMonitor)) != null) {
            if (line.isEmpty()) {
                return;
            }
            headers.addHeader(line);
        }
        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected header format of part from multipart/form-data.");
    }

    // HTTP/1.1 표준(RFC 7231 및 RFC 7578)에 따르면: multipart/form-data 요청에는 적어도 하나 이상의 파트가 포함되어야 하며, 각 파트에는 메타데이터 헤더(Content-Disposition)가 필요
    private void processParameters(MultipartHeaders headers, MultipartParameters parameters) {
        String contentDisposition = headers.getContentDisposition();
        if (contentDisposition != null) {
            parameters.addContentDispositionParameters(contentDisposition);
            return;
        }
        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Content-Disposition header not include for part from multipart/form-data format");
    }

    private void processBodyUntilBoundary(MultipartBodyUntilBoundary bodyUntilBoundary, HttpCrlfLineReader lineReader) throws IOException {
        byte[] line;
        while ((line = lineReader.readLineBytes(sizeMonitor)) != null) {
            if (boundaryValidator.isBoundary(line)|| boundaryValidator.isEndBoundary(line)) {
                bodyUntilBoundary.setBoundary(line);
                return;
            }
            bodyUntilBoundary.setBody(line);
        }
        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Boundary not found for part from multipart/form-data.");
    }

//    public boolean isLoad() {
//        return !parts.isEmpty();
//    }
//
//    public Part getPart(String name) {
//        if (parts.isEmpty()) {
//            return new Part();
//        }
//        return parts.get(name);
//    }
//
//    public void clear() throws IOException {
//        if (parts.isEmpty()) {
//            return;
//        }
//        clearParts();
//    }

//    private void storePart(MultipartHeaders headers, MultipartParameters parameters, byte[] body) throws IOException, HttpStatusException {
//        String name = parameters.getNameParamValue();
//        if (name == null) {
//            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Header has no name for part from multipart/form-data format.");
//        }
//        parts.put(name, createPart(headers, parameters, body));
//    }

    private void storePart(MultipartHeaders headers, MultipartParameters parameters, byte[] body, Request request) throws IOException {
        String name = section.getParameters().getNameParamValue();
        if (name == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Header has no name for part from multipart/form-data format.");
        }
        request.multipart().addPart(name, createPart(section.getHeaders(), section.getParameters(), section.getBody()));
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
                throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid multipart/form-data boundary value: " + boundaryValue);
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

    private static class BodyUntilBoundary {
        private final byte[] body;
        private final byte[] boundary;

        public BodyUntilBoundary(byte[] body, byte[] boundary) {
            this.body = body;
            this.boundary = boundary;
        }

        public byte[] getBody() {
            return body;
        }

        public byte[] getBoundary() {
            return boundary;
        }
    }

    private static class MultipartSection {
//        private final MultipartHeaders headers;
//        private final MultipartParameters parameters;
//        private final MultipartBodyUntilBoundary bodyUntilBoundary;
        private final MultipartHeaders headers = new MultipartHeaders();
        private final MultipartParameters parameters = new MultipartParameters();
        private final MultipartBodyUntilBoundary bodyUntilBoundary = new MultipartBodyUntilBoundary();

//        public MultipartSection(MultipartHeaders headers, MultipartParameters parameters, MultipartBodyUntilBoundary bodyUntilBoundary) {
//            this.headers = headers;
//            this.parameters = parameters;
//            this.bodyUntilBoundary = bodyUntilBoundary;
//        }

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

        public MultipartSection recycle() {
            headers.recycle();
            parameters.recycle();
            bodyUntilBoundary.recycle();
            return this;
        }
    }
}
