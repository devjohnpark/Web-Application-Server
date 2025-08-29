package org.dochi.internal.buffer;

import org.dochi.webserver.socket.SocketWrapperBase;

import java.io.IOException;

public interface InputBuffer {
    // 프로토콜별로 Buffering Logic 상이
    // HTTP/1.1: 데이터 읽어서 그대로 내부버퍼에 저장
    // HTTP/2.0: 프레임을 순서대로 조립후 HPACK 압축 해제하여 순서대로 내부 버퍼에 저장

    // buffering
    int doRead(ApplicationBufferHandler handler) throws IOException;

    // socket wrapper for input socket buffer (Any kind of socket type)
    void init(SocketWrapperBase<?> socketWrapper);

    // reset buffer and internal.Request
    void recycle();
}
