package org.dochi.internal.http11;

import org.dochi.internal.buffer.ApplicationBufferHandler;
import org.dochi.internal.buffer.InputBuffer;
import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.utils.HttpStatus;
import org.dochi.internal.parser.Http11Parser;
import org.dochi.internal.Request;
import org.dochi.webserver.socket.SocketWrapperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class Http11InputBuffer implements InputBuffer, ApplicationBufferHandler, Http11Parser.HeaderDataSource {
    private static final Logger log = LoggerFactory.getLogger(Http11InputBuffer.class);
//    private static final int SEPARATOR_SIZE = 1;
//    private static final int CRLF_SIZE = 2;
    private ByteBuffer buffer;
    private final SocketInputBuffer socketInputBuffer;
    private final Http11Parser parser;

    // Request는 AbstractProcessor 클래스에서 생성된다.
    // HttpXXInputBuffer는 AbstractProcessor 자식 클래스 Http11Processor에서 생성된다.
    // HttpXXInputBuffer는 Http11Processor에서 생성되므로 Request 객체를 생성자로 주입해서 필수 의존성 명시
    public Http11InputBuffer(int headerMaxSize) {
        this.buffer = initBuffer(headerMaxSize);
        this.socketInputBuffer = new SocketInputBuffer();
        this.parser = new Http11Parser(this);
    }

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
    public void expand(int size) { this.buffer = initBuffer(size); }

    // SocketProcessor.run() -> doRun() -> HttpProcessor를 가져와서 SocketWrapper를 초기화해서 사용
    @Override
    public void recycle() {
        this.buffer.position(0);
        this.buffer.limit(0);
    }

//    public boolean parseHeader(Request request) throws IOException {
//        try {
//            return parseRequestLine(request) && parseHeaders(request);
//        } catch (BufferOverflowException e) {
//            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Header max size exceed");
//        }
//    }

    public boolean parseHeader(Request request) throws IOException {
        try {
            return parser.parseRequestLine(request) && parser.parseHeaders(request);
        } catch (BufferOverflowException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Header max size exceed");
        }
    }

    private ByteBuffer initBuffer(int maxSize) {
        ByteBuffer buffer = ByteBuffer.allocate(maxSize);
        buffer.flip();
        return buffer;
    }

//    private int getByte() throws IOException {
//        if (!this.buffer.hasRemaining() && this.socketInputBuffer.doRead(this) <= 0) {
//            return -1;
//        }
//        return this.buffer.get() & 0xFF;
//    }

    // 1. 버퍼의 최대 크기만큼 한번에 읽어서 초기화 (하지만 HTTP Request Message가 분할 전송될수 있기 때문에 여러번 읽어야할수 있다.)


    // 현재 header buffer, payload buffer 각각 할당: payload 용 버퍼 생성 비용 발생함
    // header max size + 8kb(payload)로 초기화한 후에 해당 버퍼 계속 사용한다면 payload 용 버퍼를 생성할 필요가 없다.
    // 하지만 GC 비용을 낮추기 위해서 파싱한 객체를 따로 생성하지 않고 header buffer 를 참조해서 byte[]의 인덱스 범위로 header 요소에 접근하고 있다.

    // ApplicationBufferHandler: 매개변수를 통해 해당 메서드를 호출한 객체의 버퍼를 set/get 혹은 expand 할수 있음
    @Override
    public int doRead(ApplicationBufferHandler handler) throws IOException {
        // position >= limit
        if (!this.buffer.hasRemaining()) {
            // 1. InputBuffer 클래스의 고유 버퍼가 this.buffer로 교체된 상태: DEFAULT_BUFFER_SIZE 크기의 버퍼를 초기화해서 설정
            // 2. InputBuffer 클래스의 고유 버퍼인 상태: 용량 DEFAULT_BUFFER_SIZE 보다 작으면 expand
            if (this.buffer.array() == handler.getByteBuffer().array()) { // 복사해서 교체된 버퍼인지 확인
                handler.setByteBuffer(initBuffer(DEFAULT_BUFFER_SIZE)); // 버퍼 초기화해서 set
            } else if (handler.getByteBuffer().capacity() < DEFAULT_BUFFER_SIZE) {
                handler.expand(DEFAULT_BUFFER_SIZE);
            }
            return socketInputBuffer.doRead(handler);
        }

        // 데이터 남아있을 경우, 기존 버퍼 설정
        int length = this.buffer.remaining();

        // handler 버퍼 크기에 종속되지 않고 남은 본문 데이터가 저장된 버퍼로 교체
        handler.setByteBuffer(this.buffer.duplicate());

        // 버퍼 읽은 인덱스 업데이트
        this.buffer.position(this.buffer.limit()); // position = limit

        return length;
    }

    @Override
    public boolean fillHeaderBuffer() throws IOException {
        return this.socketInputBuffer.doRead(this) > 0;
    }

    @Override
    public ByteBuffer getHeaderByteBuffer() {
        return this.getByteBuffer();
    }

    public static class SocketInputBuffer implements InputBuffer {

        private SocketWrapperBase<?> socketWrapper;

        @Override
        public void init(SocketWrapperBase<?> socketWrapper) {
            this.socketWrapper = socketWrapper;
        }

        // SocketProcessor가 SocketWrapper을 reset하고, SocketWrapper는 socket을 감싼다.
        // 따라서 recycle 한다면, SocketWrapper = null 해도된다.

        // HttpProcessor와 함께 Http11InputBuffer는 recycle() 이후, 다른 클라이언트 요청에 사용하므로 SocketWrapper는 null값으로 설정
        // SocketProcessor에서 SocketWrapper를 reset해서 사용
        // SocketProcess.run() -> doRun() -> HttpProcessor를 가져와서 SocketWrapper를 초기화해서 사용
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
            if (bufferingLimitSize <= 0) {
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

    // String으로 파싱하면 임시 객체가 생긴다 -> GC
    // 헤더의 key가 정해져있으나 파라매터의 key는 요청마다 다르다.
    // 그리고 헤더와 다르게 파라메터는 API에서 대부분 사용되는 데이터이기 때문에 대부분 값을 파싱해서 사용된다.
    // 따라서 파라메터는 HashMap으로 저장해서 사용하도록한다.
//    private boolean parseRequestLine(Request request) throws IOException {
//        int elementCnt = 0;
//        int querySeparator = -1;
//        int previousByte = -1;
//        int currentByte;
//        int start = this.buffer.position();
//        while ((currentByte = getByte()) != -1) {
//            if (currentByte == ' ') {
//                elementCnt++;
//                if (elementCnt == 1) {
//                    request.method().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
//                } else if (elementCnt == 2) { // GET /user?name=john%20park&password=1234 HTTP/1.1
//                    request.requestURI().setCharset(StandardCharsets.UTF_8);
//                    request.requestURI().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
//                    request.requestPath().setCharset(StandardCharsets.UTF_8);
//                    if (querySeparator != -1) {
//                        request.requestPath().setBytes(buffer.array(), start, querySeparator - start - SEPARATOR_SIZE);
//                        request.queryString().setCharset(StandardCharsets.UTF_8);
//                        request.queryString().setBytes(buffer.array(), querySeparator, buffer.position() - querySeparator - SEPARATOR_SIZE);
//                    } else {
//                        request.requestPath().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
//                    }
//                }
//                start = buffer.position();
//            } else if (currentByte == '?') {
//                querySeparator = this.buffer.position();
//            } else if (previousByte == '\r' && currentByte == '\n') {
//                if (elementCnt != 2) {
//                    // 아쉬운점: 가급적 예외 객체보단 객체를 사용해서 처리한다.
//                    // 1. 스택 트레이스 캡처: 예외가 발생하면 JVM은 스택 트레이스를 캡처하는 비용 발생
//                    // 2. 예외 객체 생성 비용: 스택 트레이스까지 캡처하기 때문에 예외 객체의 생성은 일반 객체보다 비용이 큼
//                    // 3. JIT 최적화 방해: 자바 JIT(Just-In-Time) 컴파일러는 예외가 자주 발생하는 경로는 비정상 경로로 간주해서 최적화하지 않거나 인라이닝하지 않는다. 따라서 반복문 안에서 예외가 자주 발생하면 JIT 최적화가 비활성화되어 전체 성능 저하로 이어진다.
//                    // 4. 코드 실행 흐름 예측 실패: 예외는 정상적인 흐름과 분리된 별도 경로로 흐름을 변경시키기 때문에, CPU 입장에서는 분기 예측 실패와 캐시 미스 등이 더 자주 발생할 수 있다. 따라서 이 과정은 일반적인 if-else 분기보다 훨씬 무겁다.
//                    // internal.Response 객체에 HTTP Status 저장
//                    throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid request line");
//                }
//                request.protocol().setBytes(buffer.array(), start, buffer.position() - start - CRLF_SIZE);
//                return true;
//            }
//            previousByte = currentByte;
//        }
//        return false;
//    }

    // InputBuffer: Input(입력) + Buffer(버퍼) - 입력과 버퍼링을 수행하는 객체
    // HttpInputBuffer는 소켓에서 데이터를 읽고 이를 내부 버퍼에 저장하는 역할(버퍼링)을 하며, 프로토콜별 호환을 위해 기본적인 파싱 작업도 수행한다.
    // HTTP 요청을 효율적으로 처리하기 위해 '읽기(Read) + 파싱(Parse)'를 Http11InputBuffer 한 곳에서 처리
    // 예를들어 이전처럼 InputStream으로 해도룰 \r\n 단위로 읽은 뒤에 파싱을 수행한다면, 버퍼링된 데이터의 크기가 n이면 2 * n 만큼의 비용이 든다.
    // 만일, 동시 접속자의 수가 m명이라면, 2 * n * m 만큼의 비용이 발생하여 불필요한 CPU 연산이 발생한다.
    // 결론, HttpInputBuffer에서 읽기와 파싱을 함께 수행하도록 설계해서 성능 향상
    // 하지만, Http11InputBuffer의 내부적으로는 실제 데이터를 읽기(버퍼링)하는 작업을 SocketInputBuffer에게 위임해서 단일 책임(SRP)을 갖도록 한다.

    // 따라서, Http11InputBuffer가 소켓 입출력 세부사항에 의존하지 않도록 분리하여, SocketInputBuffer은 향후 다른 네트워크 계층 (ex. SSL, HTTP/2)로 확장 가능하다.
    // 이렇게 네트워크 I/O(SocketInputBuffer)와 HTTP/1.1 메시지 파싱(Http11InputBuffer)을 분리하여 유지보수성을 높이진다.
    // 결국 POST method로 본문(Ex.multipart/form-data/application/x-www-form-urlencoded)를 읽어 처리할때도, InputBuffer 구현체를 주입해서 프로토콜 별로 호환성을 가질수 있다.

    // Http11InputBuffer 파싱 범위
    // 요청 라인과 헤더를 파싱하며, 바디 데이터를 읽어 처리하는 것은 HttpExternalRequest 이다.
    // 버퍼에 남은 데이터는 외부에서 doRead 호출해서 바디 최대값으로 초기화된 바디용 버퍼에 복사한다.

    // 헤더를 버퍼에 저장해 놓고 참조해서 값을 가져오지 않는다면?
    // 헤더를 모두 파싱해서 HashMap<String, String>으로 저장해놓는다면, GC 비용(객체 생성과 해제) 증가
    // 버퍼에 저장해 놓고 참조해서 값을 가져오면, GC 비용이 줄어든다.
    // 웹서버의 트래픽이 높은 경우 높은 GC 비용을 수반하기 때문에 문제가 발생한다. GC가 실행되는 동안 애플리케이션의 모든 쓰레드가 일시 중단(stop-the-world)되기 때문이다.
    // 따라서 GC 비용을 낮춰야한다.

    // 상세2: Http11InputBuffer의 내부
    // SocketInputBuffer는 Http11InputBuffer 내부에서 소켓에서 직접 데이터를 읽고 버퍼링하는 역할을 수행.
    // 소켓에서 데이터를 읽어들여 Http11InputBuffer에서 주입한 ByteBuffer로 버퍼링을 한다.
    // Http11InputBuffer에 주입시 소켓에서 데이터를 읽는 객체의 호환성을 위해 InputBuffer을 구현한 SocketInputBuffer을 주입
    // SocketInputBuffer의 내부에서는 여러 소켓 입력에 대한 방법을 선택할수 있다. Ex) SocketInputStream/SocketChannel
    // SocketChannel을 사용하면 BufferedSocketInputStream 없이 소켓에서 데이터를 읽을 수 있다. (SocketChannel.open(): 블로킹/논블로킹 모드로 소켓 연결 가능)
    // 따라서, SocketInputBuffer에서 InputStream를 주입하는 것이 아니라, 호환되도록 추상 클래스 SocketWrapper를 주입받고 BioSocketWrapper 상속받은 객체를 주입한다.

    // ByteBuffer 사용 이유?
    // 내부적으로 byte[]를 참조하고 있으며, position, limit, capacity 필드를 사용해서 byte[]을 이어서 사용하기 좋다.
    // 동일한 byte[]을 기준으로 ByteBuffer 객체를 생성할수 있다. 따라서 ByteBuffer 인스턴스를 새로 생성하지만 기존의 동일한 byte[]를 참조할수 있도록할 수 있다.
    // ByteBuffer는 내부적으로 heap buffer 혹은 kernel buffer를 사용할수 있다. 커널 버퍼를 사용하게 될경우 애플리케이션 메모리에 할당하지 않고 다이렉트로 커널 버퍼에서 데이터를 가져오거나 보낼수 있을것이다.

    // AbstractHttpProcessor 구현체로부터 Http11InputBuffer은 SocketWrapper를 주입받아 SocketInputBuffer에 주입시켜서 생성한다. -> SocketInputBuffer(BioSocketWrapper)
    // 여러 종류(non-blocking/blocking)의 소켓을 커버하기 위해 추상 클래스 SocketWrapperBase<E socket>을 정의한다.
    // 단, SocketWrapper가 소켓 읽기/쓰기 기능을 감싸서 수행할수 있어야한다. Ex. SocketWrapperBase<E socket>: read(byte[], int off, int len)
    // 이를 구현한 blocking 소켓인 java.net.Socket을 타입의 BioSocketWrapper<Socket socket> extends SocketWrapperBase를 구현한다.
    // ByteBuffer를 사용해도 다음처럼 코드 작성이 가능하다.BioSocketWrapper<Socket socket>: read socket.getInputStream.read(buffer.array(), buffer.position, buffer.limit())
    // InputBuffer 인터페이스를 이용해서 SocketWrapperBase 객체를 매개변수로 전달한다. init(SocketWrapperBase<?> socketWrapper)

    // SocketInputBuffer를 Http11InputBuffer의 외부에서 생성하는 것은 비논리적인가?
    // SocketInputBuffer는 프로토콜에 따라 소켓 입력한 후 조립해서 HTTPXXInputBuffer의 버퍼로 버퍼링해야한다.
    // Http11InputBuffer의 입력 데이터의 경우에는 순차적으로 버퍼링되긴 하지만, HTTP/2.0의 경우는 아니다.
    // 프로토콜에 종속되는 SocketInputBuffer을 HttpXXProcessor에 주입하면 어떠할까?
    // SocketInputBuffer는 프로토콜에 종속되므로 HttpXXProcessor에서 생성해도 괜찮다.
    // 하지만 SocketInputBuffer는 HttpXXInputBuffer에 종속되므로 내부 객체로 생성하는 것이 맞다.

    // AbstractProcessor 클래스의 setSocketWrapper 추상 메서드로 Http11Processor에서 Http11InputBuffer.init(BioSocketWrapper)/Http11OutputBuffer.init(BioSocketWrapper)
    // 프로토콜 판별 -> Http11Processor 생성 -> Http11Processor 객체 생성시, Request, HttpApiRequest 객체 초기화
    // HttpXXProcessor 객체를 미리 생성할순 없을까? -> 기본 개수만 생성하고 초과하면 동적으로 2배씩 확장? or 동적으로 하나씩 생성하고 재활용
    // AbstractProtocol 클래스에서 SynchronizedStack 객체를 사용해서 HttpXXProcessor 객체를 push(), pop()
    // AbstractProcessor 클래스도 AbstractProtocol 클래스에서 recycle를 통해 초기화해서 재활용
    // AbstractProcessor을 재활용하기 위해서 해당 클래스에 setSocketWrapper(BioSocketWrapper) 메서드를 정의하였다.
    // 그리고 HttpXXInputBuffer 재활용하기 위해서 해당 클래스에 init(BioSocketWrapper) 메서드를 정의하였다.
    // 소켓 데이터의 읽기는 SocketWrapper가 책임지므로 SocketInputBuffer의 로직을 Http11inputBuffer에 위임하는 것이 합리적이고 레이어 줄이고 객체 생성비용을 아낀다.

//    public boolean parseHeaders(Request request) throws IOException {
//        HeaderParseStatus status;
//        do {
//            status = parseHeaderField(request);
//        } while (status == HeaderParseStatus.NEED_MORE); // DONE, EOF
//        return status == HeaderParseStatus.DONE && request.headers().size() > 0;
//    }
//
//    private HeaderParseStatus parseHeaderField(Request request) throws IOException {
//        int previousByte = -1;
//        int currentByte;
//        int nameStart = buffer.position();
//        int nameEnd = nameStart;
//        int valueStart = nameStart;
//        int valueEnd = nameStart;
//        while ((currentByte = getByte()) != -1) { // 1 2
//            if (currentByte == ':' && nameStart == nameEnd) { // && buffer.position() > nameStart + 1 &&
//                if (buffer.position() <= nameStart + 1) {
//                    break;
//                }
//                nameEnd = buffer.position() - 1;
//                valueStart = buffer.position();
//            } else if (previousByte == ':' && (currentByte == ' ' || currentByte == '\t')) {
//                valueStart++;
//            } else if (previousByte == '\r' && currentByte == '\n') {
//                valueEnd = buffer.position() - 2;
//                if (nameStart < nameEnd && nameEnd < valueStart && valueStart < valueEnd) {
//                    MimeHeaderField headerField = request.headers().createHeader();
//                    headerField.getName().setBytes(buffer.array(), nameStart, nameEnd - nameStart);
//                    headerField.getValue().setBytes(buffer.array(), valueStart, valueEnd - valueStart);
//                    return HeaderParseStatus.NEED_MORE;
//                } else if (nameStart == valueEnd) {
//                    return HeaderParseStatus.DONE;
//                }
//                break;
//            }
//            previousByte = currentByte;
//        }
//        if (currentByte == -1) {
//            return HeaderParseStatus.EOF;
//        }
//        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid request header");
//    }

//    private static enum HeaderParseStatus {
//        DONE,
//        NEED_MORE,
//        EOF;
//        private HeaderParseStatus() {
//        }
//    }
}
