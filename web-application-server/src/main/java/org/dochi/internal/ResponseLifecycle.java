package org.dochi.internal;

import java.io.OutputStream;

// 응답 객체에서 구현하는 공통의 출력 버퍼링 및 직렬화 객체 주입과 재사용 인터페이스
public interface ResponseLifecycle {
    void init(OutputStream outputStream);
    void recycle();
}
