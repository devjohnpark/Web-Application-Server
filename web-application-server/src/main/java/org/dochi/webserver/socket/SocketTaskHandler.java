package org.dochi.webserver.socket;

import org.dochi.internal.processor.HttpProcessor;
import org.dochi.webserver.protocol.HttpProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

// 클라이언트(소켓)당 스레드 하나가 공유 자원이 없는 작업을 처리하기 때문에 동기화 로직은 필요없다.
public class SocketTaskHandler implements SocketTask {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskHandler.class);
    private SocketWrapper<?> socketWrapper;
    private final HttpProtocolHandler protocolHandler;

    public SocketTaskHandler(HttpProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    @Override
    public void run() {
        try {
            SocketState state = SocketState.OPEN;
            HttpProcessor processor = this.protocolHandler.getProcessor(); // 기본 default HTTP/1.1;
            while (state == SocketState.OPEN) {
                state = processor.process(socketWrapper);
                if (state == SocketState.CLOSED) {
                    protocolHandler.release(processor);
                } else if (state == SocketState.UPGRADING) {
                    // 1. 파싱된 요청 데이터 객체(internal.RequestHeader)의 복사본을 가지고 헤더에서 h2 관련 데이터 가져와서(AbstractProcessor.getUpgradeToken()) HTTP/2 설정
                    // 2. 필요한 스트림의 개수 만큼 Http2Processor 생성
                    // 3. 소켓 연결 시간 다시 설정

                    // HTTP/2.0은 다수의 스트림을 필요하므로 다수의 HttpProcessor가 필요하다.
                    // 땨라서 반복문으로 필요한 스트림 개수만큼 HttpProcessor 생성
                    // processor = this.protocolHandler.getProcessor("HTTP/2.0");
                    // HttpProcessor.process() 비동기로 전환 필요
                }
            }
        } finally {
            terminate();
        }
    }

    private void terminate() {
        try {
            getSocketWrapper().close();
        } catch (IOException e) {
            log.error("Failed to close socket - Socket State CLOSED: {}", e.getMessage());
        } finally {
            socketWrapper = null; // SocketTaskHandler 구현체는 풀링되어 큐에 저장되므로 SocketWrapper 구현체가 메모리 낭비되므로 null 값 할당
        }
    }

    @Override
    public SocketWrapper<?> getSocketWrapper() {
        if (socketWrapper == null) {
            throw new IllegalStateException("Socket wrapper is not initialized");
        }
        return socketWrapper;
    }

    @Override
    public void setSocketWrapper(SocketWrapper<?> socketWrapper) {
        if (socketWrapper == null) {
            throw new IllegalStateException(getClass().getName() + ": Socket wrapper is null");
        }
        this.socketWrapper = socketWrapper;
    }
}
