package org.dochi.internal.buffer;

import java.nio.ByteBuffer;

// 실제 입출력을 수행하는 객체에서 구현쳉의 버퍼를 역제어하기 위한 인터페이스
public interface ApplicationBufferHandler {

    int DEFAULT_BUFFER_SIZE = 8192; // body 용 버퍼를 설정할때 기본 8kb로 설정
    ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0); // 버퍼 교체를 위한 임시 초기화 버퍼

    // 실제 입출력을 수행하는 객체에서 구현체의 버퍼 설정
    void setByteBuffer(ByteBuffer buffer);

    // 실제 입출력을 수행하는 객체에서 구현체의 버퍼 가져오기
    ByteBuffer getByteBuffer();
}
