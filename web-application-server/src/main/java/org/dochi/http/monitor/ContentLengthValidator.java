package org.dochi.http.monitor;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.response.HttpStatus;

import java.io.IOException;

public class ContentLengthValidator {

    private final ContentLengthMonitor contentLengthMonitor;

    public ContentLengthValidator(ContentLengthMonitor contentLengthMonitor) {
        this.contentLengthMonitor = contentLengthMonitor;
    }

    private int validatePreContentLength(int contentLength) {
        return validateMaxContentLength(validateContentLength(contentLength), contentLengthMonitor.getMaxContentLength());
    }

    private int validateMaxContentLength(int contentLength, int maxContentLength) {
        if (contentLength > maxContentLength) {
            throw new HttpStatusException(HttpStatus.PAYLOAD_TOO_LARGE, String.format("RequestBody max size exceeded [Content-Length: %d, Max Content-Length: %d]", contentLength, maxContentLength));
        }
        return contentLength;
    }

    private int validateContentLength(int contentLength) {
        if (contentLength == -1) { // Header에 Content-Length 값이 저장된 적인 없는 상태
            throw new HttpStatusException(HttpStatus.LENGTH_REQUIRED);
        }
        return contentLength;
    }

    private void validateActualContentLength(int expectedContentLength) {
        int actualContentLength = contentLengthMonitor.getActualContentLength();
        if (expectedContentLength != actualContentLength) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("Content-Length mismatched [Content-Length: %d, Actual Content-Length: %d]", expectedContentLength, actualContentLength));
        }
    }

//    public <T> T validateContentOnRead(int contentLength, ContentReader<T> reader) throws IOException {
//        int validatedLength = validatePreContentLength(contentLength);
//        T result = reader.read(validatedLength);
//        validateActualContentLength(validatedLength);
//        return result;
//    }

    public <T> T validateContentOnRead(int contentLength, ContentReader<T> reader) throws IOException {
        int validatedLength = validatePreContentLength(contentLength);
        T result = reader.read(validatedLength);
        validateActualContentLength(validatedLength);
        return result;
    }

    @FunctionalInterface
    public interface ContentReader<T> {
        T read(int contentLength) throws IOException;
    }

//    public <T> T validateWhenGetContent(ContentReader<T> reader, int contentLength) throws IOException, HttpStatusException {
//        int validatedContentLength = validatePreContentLength(contentLength);
//        T result = reader.read(validatedContentLength);
//        validateActualContentLength(validatedContentLength);
//        return result;
//    }
//
//    // 인터페이스가 해당 클래스의 동작에만 의존하므로 이를 외부로 노출하는 것보다 내부에서 정의하는 것이 범위를 제한한다.
//    @FunctionalInterface
//    public interface ContentReader<T> {
//        T read(int contentLength) throws IOException, HttpStatusException;
//    }
}
