package org.dochi.internal.http11;

import org.dochi.internal.buffer.ApplicationBufferHandler;
import org.dochi.internal.buffer.InputBuffer;
import org.dochi.internal.RequestMetadata;
import org.dochi.webserver.socket.SocketWrapperBase;
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

    // AbstractHttpProcessor 구현체로부터 Http11InputBuffer은 SocketWrapper를 주입받아 SocketInputBuffer에 주입시켜서 생성한다. -> SocketInputBuffer(BioSocketWrapper)
    // 여러 종류(non-blocking/blocking)의 소켓을 커버하기 위해 추상 클래스 SocketWrapperBase<E socket>을 정의한다.
    // 단, SocketWrapper가 소켓 읽기/쓰기 기능을 감싸서 수행할수 있어야한다. Ex. SocketWrapperBase<E socket>: read(byte[], int off, int len)
    // InputBuffer 인터페이스를 이용해서 SocketWrapperBase 객체를 매개변수로 전달한다. init(SocketWrapperBase<?> socketWrapper)
    @Override
    public void init(SocketWrapperBase<?> socketWrapper) {
        if (socketWrapper == null) {
            log.debug("socketWrapper cannot be null");
            throw new IllegalArgumentException("socketWrapper cannot be null");
        }
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
    boolean parseHeader(RequestMetadata requestMetadata) throws IOException {
        return parser.parseRequestLine(requestMetadata) && parser.parseHeaders(requestMetadata);
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

        private SocketWrapperBase<?> socketWrapper;

        @Override
        public void init(SocketWrapperBase<?> socketWrapper) {
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
                throw new IllegalArgumentException("buffer is null");
            }
            int bufferingLimitSize = buffer.capacity() - buffer.limit();
            if (bufferingLimitSize <= 0) { // 버퍼 용량 초과하기 이전에 예외 발생
                throw new BufferOverflowException();
            }
            if (this.socketWrapper == null) {
                throw new IllegalStateException("No socket wrapper is initialized");
            }
            int bytesRead = this.socketWrapper.read(buffer.array(), buffer.limit(), bufferingLimitSize);
            if (bytesRead > 0) {
                // 읽은 데이터 크기만큼 기존 limit 증가
                buffer.limit(buffer.limit() + bytesRead);
            }
            return bytesRead;
        }
    }
}
