package org.dochi.internal.http11;

import org.dochi.internal.RequestHeader;
import org.dochi.internal.buffer.ApplicationBufferHandler;
import org.dochi.internal.buffer.InputBuffer;
import org.dochi.webserver.socket.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class Http11InputBuffer implements InputBuffer, ApplicationBufferHandler, Http11Parser.HeaderDataSource {
    private static final Logger log = LoggerFactory.getLogger(Http11InputBuffer.class);
    private ByteBuffer buffer;
    private final SocketInputBuffer socketInputBuffer;
    private final Http11Parser parser;

    public Http11InputBuffer(int headerMaxSize) {
        this.buffer = ByteBuffer.allocate(headerMaxSize + DEFAULT_BUFFER_SIZE);
        this.buffer.flip();
        this.socketInputBuffer = new SocketInputBuffer();
        this.parser = new Http11Parser(this, headerMaxSize);
    }

    @Override
    public void init(SocketWrapper<?> socketWrapper) {
        this.socketInputBuffer.init(socketWrapper);
    }

    @Override
    public void setByteBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public ByteBuffer getByteBuffer() { return this.buffer; }

    @Override
    public void recycle() {
        this.buffer.position(0);
        this.buffer.limit(0);
    }

    // 내부 구현이므로 같은 http11 패키지내의 클래스만 호출가능하도록 패키지 전용 접근 제어자로 지정
    boolean parseHeader(RequestHeader requestHeader) throws IOException {
        return parser.parseRequestLine(requestHeader) && parser.parseHeaders(requestHeader);
    }

    @Override
    public int doRead(ApplicationBufferHandler handler) throws IOException {

        // ByteBuffer.duplicate() 만 해도 기존 버퍼를 그대로 참조하면서 아직 읽지 않은 구간을 잘라서 독립적으로 사용 가능
        // 그렇다면 그대로 가져다가 InputBuffer 클래스에서 recycle(pos = 0, limit = 0) 해도 무관 -> 버퍼의 헤더 부분은 버퍼링 유지

        // handler.getByteBuffer(): ApplicationBufferHandler 구현체의 버퍼를 가져옴(여기서는 payload buffer를 뜻)
        if (handler.getByteBuffer() == EMPTY_BUFFER)  // 아직 구현체의 버퍼 교체가 안되어 있으면
            handler.setByteBuffer(this.buffer.duplicate()); // 헤더 버퍼링 후 남은 버퍼 용량을 독립적으로 사용할수있도록 설정

        if (!handler.getByteBuffer().hasRemaining()) // Application 버퍼에 읽지 않은 데이터가 없다면
            return socketInputBuffer.doRead(handler); // 소켓 버퍼에서 Application 버퍼로 버퍼링

        return handler.getByteBuffer().remaining(); // 읽지 않은 데이터 크기 반환
    }

    @Override
    public boolean fillHeaderBuffer() throws IOException {
        return this.socketInputBuffer.doRead(this) > 0;
    }

    @Override
    public ByteBuffer getHeaderByteBuffer() {
        return this.getByteBuffer();
    }

    private static class SocketInputBuffer implements InputBuffer {

        private SocketWrapper<?> socketWrapper;

        @Override
        public void init(SocketWrapper<?> socketWrapper) {
            if (socketWrapper == null) {
                log.debug("socketWrapper cannot be null");
                throw new IllegalArgumentException("socketWrapper cannot be null");
            }
            this.socketWrapper = socketWrapper;
        }

        @Override
        public void recycle() {
            this.socketWrapper = null;
        }

        @Override
        public int doRead(ApplicationBufferHandler handler) throws IOException {
            ByteBuffer buffer = handler.getByteBuffer();
            if (buffer == null) {
                throw new IllegalStateException("buffer is null");
            }
            if (this.socketWrapper == null) {
                throw new IllegalStateException("No socket wrapper is initialized");
            }
            int bytesRead = this.socketWrapper.read(buffer.array(), buffer.limit(), buffer.capacity() - buffer.limit());
            if (bytesRead > 0) {
                // 읽은 데이터 크기만큼 기존 limit 증가
                buffer.limit(buffer.limit() + bytesRead);
            }
            return bytesRead;
        }
    }
}
