package org.dochi.internal.http11;

import org.dochi.internal.buffer.ApplicationBufferHandler;
import org.dochi.internal.buffer.InputBuffer;
import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.utils.HttpStatus;
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
    private final int headerMaxSize;

    public Http11InputBuffer(int headerMaxSize) {
        this.headerMaxSize = headerMaxSize;
        this.buffer = ByteBuffer.allocate(headerMaxSize + DEFAULT_BUFFER_SIZE);
        this.buffer.flip();
        this.socketInputBuffer = new SocketInputBuffer();
        this.parser = new Http11Parser(this);
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
        if (!parser.parseRequestLine(requestMetadata) || !parser.parseHeaders(requestMetadata)) {
            return false;
        }
        // 헤더의 끝을 읽었는데 헤더 최대 크기보다 클때, 헤더 최대 사이즈 초과 예외 던진다.
        if (buffer.position() > headerMaxSize) {
            log.warn("Header parsing completed but exceeded maximum header size limit: pos = {}, limit = {}", buffer.position(), buffer.limit());
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Header max size exceed");
        }
        return true;
    }

    // ByteBuffer 사용 이유?
    // 내부적으로 byte[]를 참조하고 있으며, position, limit, capacity 필드를 사용해서 byte[]을 이어서 읽고 재활욯하기 편하다.
    // 동일한 byte[]을 기준으로 ByteBuffer 객체를 생성할수 있다. 따라서 ByteBuffer 인스턴스를 새로 생성하지만 기존의 동일한 byte[]를 참조할수 있도록할 수 있다.
    // ByteBuffer는 내부적으로 heap buffer 혹은 direct buffer를 사용할수 있다.
    // 추후 다이렉트 버퍼로 변경시 이점
    // 읽기: 커널 영역인 TCP 버퍼에서 유저 영역(app)인 Heap 복사를 거치지 않고 다이렉트로 디스크에 저장할수있다.
    // 쓰기: Heap 복사를 거치지 않고 OS가 바로 TCP 버퍼로 복사가 가능해서 디스크에 저장하던것을 바로 전송할수있다.
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
        try {
            return this.socketInputBuffer.doRead(this) > 0;
        } catch (BufferOverflowException e) {
            // 헤더 버퍼링시 오버 플로우에 대한 처리: 헤더 끝을 못읽은 채로 버퍼 사이즈 초과
            log.warn("Buffer size exceeded before completing header parsing: limit = {}, capacity = {}", buffer.limit(), buffer.capacity());
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Header max size exceed");
        }
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
