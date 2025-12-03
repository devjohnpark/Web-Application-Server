package org.dochi.internal;

import org.dochi.internal.buffer.InputBuffer;

// 요청 객체에서 구현하는 공통의 입력 버퍼링 및 파싱 객체 주입과 재사용 인터페이스
public interface RequestLifecycle {
    void init(InputBuffer inputBuffer); // 요청 처리 시작: 입력 버퍼 설정
    void recycle(); // 요청 처리 종료: 초기화
}
