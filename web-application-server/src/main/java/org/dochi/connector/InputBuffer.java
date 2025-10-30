package org.dochi.connector;

import org.dochi.internal.buffer.ApplicationBufferHandler;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

// 클라이언트(소켓)당 스레드 하나가 공유 자원이 없는 작업을 처리하기 때문에 동시성이 없어서 BufferedInputStream처럼 동기화 로직은 필요없다.
// 따라서 동기화 기능이 없기 때문에 BufferedSocketInputStream 보다 읽기 속도가 더 빠르다.
public class InputBuffer implements ApplicationBufferHandler, Closeable {
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
            return internalInputBuffer.doRead(this); // 헤더가 알아서 버퍼 조작
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
