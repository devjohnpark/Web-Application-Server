package org.dochi.internal.buffer;

import org.dochi.net.AbstractSocketWrapper;

import java.io.IOException;

// 실제 입력 버퍼링을 수행하는 객체에 역할 부여
public interface InputBuffer {
    // buffering
    int doRead(ApplicationBufferHandler handler) throws IOException;

    // socket wrapper for input socket buffer (Any kind of socket type)
    void init(AbstractSocketWrapper<?> socketWrapper);

    // reset buffer
    void recycle();
}
