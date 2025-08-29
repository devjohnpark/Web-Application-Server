package org.dochi.internal.buffer;

import java.nio.ByteBuffer;

// 프로토콜 별 소켓 입력을 수행하는 internal.InputBuffer 인터페이스의 doRead() 메서드는 internal 계층의 종속된다.
// 따라서 WAS 내부의 특정 클래스만 InputBuffer 구현체의 doRead() 메서드를 호출할수 있어야한다.
// 그래서 ApplicationBufferHandler 인터페이스를 정의하고 해당 인테페이스를 구현한 클래스만 internal.InputBuffer.doRead() 메서드를 호출할수 있게 한다.
// internal.InputBuffer.doRead(ApplicationBufferHandler) 처럼 매개변수를 인터페이스로 정의하면, ApplicationBufferHandler 구현체만 InputBuffer 구현체의 doRead() 메서드를 호출할수 있다.
// 또한 InputBuffer 구현체에서는 doRead 메서드의 전달받은 매개변수인 ApplicationBufferHandler 구현체의 내부 버퍼를 핸들링 할수 있게된다.
// HTTP/1.1에 대한 요쳉 메세지를 버퍼링과 파싱을 수행하는 InputBuffer를 구현한 Http11InputBuffer에서 헤더 부분을 버퍼링 하다가 바디 부분의 데이터까지 버퍼링된다.
// 그려면 바디 부분을 입력하는 클래스에서 InputBuffer를 구현체의 내부 버퍼에서 바디 부분의 데이터를 가져와야한다.
// 결국, 바디 부분을 입력하는 ApplicationBufferHandler 구현체는 InputBuffer를 구현체의 doRead(ApplicationBufferHandler)의 매개변수를 통해 다음의 선택지가 있다.
// 1. getByteBuffer(ByteBuffer buffer): InputBuffer 구현체에서 바디 부분도 버퍼링된 ByteBuffer를 복사하기 위해서 ApplicationBufferHandler 구현체의 버퍼를 가져온다.
// 2. setByteBuffer(ByteBuffer buffer): InputBuffer 구현체에서 바디 부분도 버퍼링된 ByteBuffer에서 데이터를 복사해서 새로운 ByteBuffer 생성해서 ApplicationBufferHandler 구현체의 버퍼를 설정한다.
// 따라서 ApplicationBufferHandler 구현체의 내부 버퍼의 핸들링이 가능하도록 인터페이스를 정의하였다.
// 또한 ApplicationBufferHandler 구현체의 내부 버퍼가 작다면 확장할수 있는 기능이 필요했다.: expand(int size)

// 그러면 ApplicationBufferHandler 구현체에서 InputBuffer.doRead(ApplicationBufferHandler)를 호출할때, InputBuffer 구현체에서 ApplicationBufferHandler 구현체의 내부 버퍼로 버퍼링을 수행할수 있다.
// 이를 통해, 바디용 버퍼가 존재하고 바디를 버퍼링하는 connector.InputBuffer 클래스에서 내부적으로 internal.InputBuffer 구현체인 Http11InputBuffer를 통해 바디용 버퍼로 버퍼링을 수행 가능하다.

public interface ApplicationBufferHandler {

    int DEFAULT_BUFFER_SIZE = 8192; // 바디용 버퍼를 설정할때 기본 8kb로 설정
    ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0); // 버퍼 교체를 위한 임시 초기화 버퍼

    void setByteBuffer(ByteBuffer buffer);

    ByteBuffer getByteBuffer();

    void expand(int size);
}
