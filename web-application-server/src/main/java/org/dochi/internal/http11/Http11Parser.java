package org.dochi.internal.http11;

import org.dochi.internal.RequestMetadata;
import org.dochi.internal.buffer.MimeHeaderField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Http11Parser {
    private static final Logger log = LoggerFactory.getLogger(Http11Parser.class);
    private static final int SEPARATOR_SIZE = 1;
    private static final int CRLF_SIZE = 2;
    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final byte WHITE_SPACE = ' ';
    private static final byte TAB = '\t';
    private static final byte QUERY_SP = '?';
    private static final byte HEADER_KEY_VALUE_SP = ':';
    private final HeaderDataSource source;
    private final int headerMaxSize;

    public Http11Parser(HeaderDataSource source, int headerMasSize) {
        this.source = source;
        this.headerMaxSize = headerMasSize;
    }

    private byte getByte() throws IOException {
        if (!this.source.getHeaderByteBuffer().hasRemaining() && !this.source.fillHeaderBuffer()) {
            return -1;
        }

        System.out.println("header position, limit" + this.source.getHeaderByteBuffer().position() + " " + this.source.getHeaderByteBuffer().limit());
        if (this.source.getHeaderByteBuffer().position() > headerMaxSize) {
            throw new IllegalStateException("header size exceeded");
        }
        return this.source.getHeaderByteBuffer().get();
    }

    public boolean parseRequestLine(RequestMetadata requestMetadata) throws IOException {
        int elementCnt = 0;
        int querySeparator = -1;
        byte previousByte = -1;
        byte currentByte;
        ByteBuffer buffer = source.getHeaderByteBuffer();
        int start = buffer.position();
        while ((currentByte = getByte()) != -1) {
            if (currentByte == WHITE_SPACE) {
                elementCnt++;
                if (elementCnt == 1) {
                    requestMetadata.method().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
                } else if (elementCnt == 2) {
                    requestMetadata.requestURI().setCharset(StandardCharsets.UTF_8);
                    requestMetadata.requestURI().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
                    requestMetadata.requestPath().setCharset(StandardCharsets.UTF_8);
                    if (querySeparator != -1) {
                        requestMetadata.requestPath().setBytes(buffer.array(), start, querySeparator - start - SEPARATOR_SIZE);
                        requestMetadata.queryString().setCharset(StandardCharsets.UTF_8);
                        requestMetadata.queryString().setBytes(buffer.array(), querySeparator, buffer.position() - querySeparator - SEPARATOR_SIZE);
                    } else {
                        requestMetadata.requestPath().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
                    }
                }
                start = buffer.position();
            } else if (currentByte == QUERY_SP && querySeparator == -1) {
                querySeparator = buffer.position();
            } else if (previousByte == CR && currentByte == LF) {
                if (elementCnt != 2) {
                    throw new IllegalArgumentException("Invalid requestMetadata line");
                }
                requestMetadata.protocol().setBytes(buffer.array(), start, buffer.position() - start - CRLF_SIZE);
                return true;
            }
            previousByte = currentByte;
        }
        return false;
    }

    public boolean parseHeaders(RequestMetadata requestMetadata) throws IOException {
        HeaderParseStatus status;
        do {
            status = parseHeaderField(requestMetadata);
        } while (status == HeaderParseStatus.NEED_MORE);
        return status == HeaderParseStatus.DONE && requestMetadata.headers().size() > 0;
    }

    private HeaderParseStatus parseHeaderField(RequestMetadata requestMetadata) throws IOException {
        byte previousByte = -1;
        byte currentByte;
        ByteBuffer buffer = source.getHeaderByteBuffer();
        int nameStart = buffer.position();
        int nameEnd = nameStart;
        int valueStart = nameStart;
        int valueEnd = nameStart;
        
        while ((currentByte = getByte()) != -1) { // 1 2
            if (currentByte == HEADER_KEY_VALUE_SP && nameStart == nameEnd) { // && buffer.position() > nameStart + 1 &&
                if (buffer.position() <= nameStart + 1) {
                    break;
                }
                nameEnd = buffer.position() - 1;
                valueStart = buffer.position();
            } else if (previousByte == HEADER_KEY_VALUE_SP && (currentByte == WHITE_SPACE || currentByte == TAB)) {
                valueStart++;
            } else if (previousByte == CR && currentByte == LF) {
                valueEnd = buffer.position() - 2;
                if (nameStart < nameEnd && nameEnd < valueStart && valueStart < valueEnd) {
                    MimeHeaderField headerField = requestMetadata.headers().createHeader();
                    headerField.name().setBytes(buffer.array(), nameStart, nameEnd - nameStart);
                    headerField.getValue().setBytes(buffer.array(), valueStart, valueEnd - valueStart);
                    return HeaderParseStatus.NEED_MORE;
                } else if (nameStart == valueEnd) {
                    return HeaderParseStatus.DONE;
                }
                break;
            }
            previousByte = currentByte;
        }
        if (currentByte == -1) {
            return HeaderParseStatus.EOF;
        }
        throw new IllegalArgumentException("Invalid requestMetadata header");
    }

    private enum HeaderParseStatus {
        DONE,
        NEED_MORE,
        EOF;
    }

    // HeaderDataSource 인터페이스를 HttpHeaderParser 내에 정의하여, HeaderDataSource 구현체가 HttpHeaderParser에서만 사용하는것을 의미
    public interface HeaderDataSource {

        boolean fillHeaderBuffer() throws IOException;

        ByteBuffer getHeaderByteBuffer();
    }
}