package org.dochi.connector;

import org.dochi.internal.buffer.ApplicationBufferHandler;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

// java 프로그래머한테 InputStream이 익숙하므로 InputBuffer를 BufferedSocketInputStream/Reader로 감싸서 제공
// 헤더를 버퍼링할때 ByteBuffer에서 버퍼링된 바디의 데이터를 복사
// Reader의 경우 byte[]를 char[]로 변환하는 과정이 필요함
// Request는 Reader/BufferedSocketInputStream 객체로 개발자에게 제공
// Response는 Writer/OutputStream 객체로 개발자에게 제공

// InternalInputStream 객체는 내부적으로 프로토콜별 호환성을 지닌 입력 객체를 포함하는 connector.InputBuffer 객체를 참조한다.
// connector.InputBuffer 객체는 connector.Request 객체에 포함되어 있으며, 재활용할수 있는 객체이다.
// 반면에 InternalInputStream은 java의 여타 BufferedSocketInputStream 상속 클래스 처럼 한번 생성되고 소모되는 객체이다.
// 따라서 connector.InputBuffer 객체는 InputStream을 상속하는 InternalInputStream의 Closeable 인터페이스를 구현해야한다.

// connector.InputBuffer -> internal.Request -> internal.InputBuffer
// 내부적으로 프로토콜 별로 호환되는 internal.InputBuffer 구현체를 참조하는 internal.Request 객체로 HTTP/1.1 Body 혹은 HTTP/2.0 Data Frame을 입력 + 버퍼링을 수행하는 객체

// 클라이언트당 스레드가 할당되기 때문에 동기화 이슈는 없어서 BufferedInputStream처럼 동시성 로직은 필요없음
// 동시성 기능이 없기 때문에 BufferedSocketInputStream 보다 읽기 속도가 더 빠르다.
public class InputBuffer implements ApplicationBufferHandler, Closeable {
//    private Request request;
    private org.dochi.internal.buffer.InputBuffer internalInputBuffer;
    private ByteBuffer buffer; // 시스템 콜 비용을 줄이기 위해서 내부 버퍼 사용
    private boolean isClosed;

    protected InputBuffer() {
        this(EMPTY_BUFFER);
    }

    protected InputBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        this.isClosed = false;
    }

    // 지연 초기화(lazy init)와 internal.Request와 connector.InputBuffer의 생명주기 분리
    public void setInputBuffer(org.dochi.internal.buffer.InputBuffer internalInputBuffer) {
        this.internalInputBuffer = internalInputBuffer;
    }

    @Override
    public void setByteBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return this.buffer;
    }

    @Override
    public void expand(int size) {
        if (this.buffer == EMPTY_BUFFER) {
            this.buffer = ByteBuffer.allocate(size);
            this.buffer.flip();
        }
    }

    public void recycle() {
        this.isClosed = false;
        this.buffer = EMPTY_BUFFER;
    }

    // 자동으로 파싱하는 Multipart/form-data나 application/x-www-form-urlencoded 본문을 파싱한다면 입력스트림을 이미 한번 사용한것이므로 소모 되어야한다.
    // 요청을 하나 처리할때마다 close 호출 (try-with-resources 구문 사용하면 close 자동 호출)
    @Override
    public void close() {
        this.isClosed = true;
    }

    private int fill() throws IOException {
        // 버퍼에 남은 데이터가 없으면, position과 limit을 0으로 설정하고 버퍼링 수행
        if (!this.buffer.hasRemaining()) { // pos >= limit
            this.buffer.position(0);
            this.buffer.limit(0);
            return internalInputBuffer.doRead(this);
        }
        // 버퍼에 아직 읽지 않은 데이터가 있다면 0 반환
        return 0;
    }

    private void throwIfClosed() throws IOException {
        if (isClosed) {
            throw new IOException("InternalInputStream is closed");
        }
    }

    public int read() throws IOException {
        throwIfClosed();
        if (fill() < 0) {
            return -1;
        }
        return this.buffer.get() & 0xFF;
    }


    public int read(byte[] b) throws IOException {
        throwIfClosed();
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        throwIfClosed();
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        if (fill() < 0) {
            return -1;
        }
        int n = Math.min(this.buffer.remaining(), len);
        // System.arraycopy()가 ByteBuffer.get()보다 네이티브 최적화 덕분에 실제 실행 속도가 더 빠르다.
        // 그러나 ByteBuffer.get()은 채널 기반 입출력인 FileChannel, SocketChannel을 사용한다면, heap 영역 데이터 복사를 커치지 않고 네트워크나 파일 I/O시에 커널영역으로 직접 데이터를 복사하므로 더 빠르다.
        // 향후 SocketChannel을 디폴트로 사용하도록 할 예정
        this.buffer.get(b, off, n);
        return n;
    }
}
