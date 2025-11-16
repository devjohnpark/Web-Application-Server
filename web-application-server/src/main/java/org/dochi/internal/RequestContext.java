package org.dochi.internal;

import org.dochi.internal.buffer.InputBuffer;

// 요청 처리에 필요한 최소한의 컨텍스트
public interface RequestContext {
    void setInputBuffer(InputBuffer inputBuffer); // 요청 처리 시작: 입력 버퍼 설정
    void recycle(); // 요청 처리 종료: 초기화
}
