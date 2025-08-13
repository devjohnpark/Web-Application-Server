package org.dochi.http.monitor;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.response.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpMessageSizeManager {
    private static final Logger log = LoggerFactory.getLogger(HttpMessageSizeManager.class);
    private int totalHeaderSize = 0;
    private int totalBodySize = 0;

    private final int headerMaxSize;
    private final int bodyMaxSize;

    public HttpMessageSizeManager(int headerMaxSize, int bodyMaxSize) {
        this.headerMaxSize = headerMaxSize;
        this.bodyMaxSize = bodyMaxSize;
    }

    private final MessageSizeMonitor header = new MessageSizeMonitor() {
        @Override
        public int getSizeLimit() { return getHeaderMaxSize(); }

        @Override
        public void monitorSize(int size){ addHeaderSize(size); }
    };

    private final MessageSizeMonitor body = new MessageSizeMonitor() {
        @Override
        public int getSizeLimit() {
            return getBodyMaxSize();
        }

        @Override
        public void monitorSize(int size)  { addBodySize(size); }
    };
    
    private final ContentLengthMonitor content = new ContentLengthMonitor() {
        @Override
        public int getActualContentLength() { return getTotalBodySize(); }

        @Override
        public int getMaxContentLength() { return getBodyMaxSize(); };
    };

    private void addHeaderSize(int currentSize) {
        validateHeaderMaxSize(totalHeaderSize += currentSize);
    }

    private void addBodySize(int currentSize) {
        validateBodyMaxSize(totalBodySize += currentSize);
    }

    private void validateHeaderMaxSize(int validSize) {
        if (validSize > headerMaxSize) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("Request Header max size exceeded [Max Header Size: %d]", headerMaxSize));
        }
    }

    private void validateBodyMaxSize(int validSize) {
        if (validSize > bodyMaxSize) {
            throw new HttpStatusException(HttpStatus.PAYLOAD_TOO_LARGE, String.format("Request Body max size exceeded [Max Content-Length: %d]", bodyMaxSize));
        }
    }

    private int getTotalHeaderSize() {
        return totalHeaderSize;
    }

    private int getHeaderMaxSize() { return headerMaxSize; }

    private int getTotalBodySize() {
        return totalBodySize;
    }

    private int getBodyMaxSize() {
        return bodyMaxSize;
    }

    public MessageSizeMonitor getHeaderMonitor() { return header; }

    public MessageSizeMonitor getBodyMonitor() { return body; }
    
    public ContentLengthMonitor getContentMonitor() { return content; }

    public void clear() {
        totalHeaderSize = 0;
        totalBodySize = 0;
    }
}
