package org.dochi.webserver.executor;

import org.dochi.connector.InternalAdapter;
import org.dochi.webserver.protocol.HttpProtocolHandler;
import org.dochi.webserver.config.*;
import org.dochi.webserver.socket.SocketTaskHandler;
import org.dochi.webserver.socket.SocketTaskPool;

public class SocketTaskExecutorFactory {
    private static final SocketTaskExecutorFactory INSTANCE = new SocketTaskExecutorFactory();

    private SocketTaskExecutorFactory() {}

    public static SocketTaskExecutorFactory getInstance() {
        return INSTANCE;
    }

    // Connector에 공통의 서버 설정 값을 들고있는다.
    // Adapter에는 Connector를 주입
    // HttpProtocolHandler에는 각 프로토콜별로 상이한 설정값을 주입
    // HttpProtocolHandler에서 설정 값과 함꼐 각 프로토콜 별 입력 버퍼링 및 파서 객체 생성
    public SocketTaskPoolExecutor createExecutor(ServerConfig serverConfig) {
        return createSocketTaskExecutor(
            serverConfig.getThreadPool(),
            new HttpProtocolHandler(new InternalAdapter(serverConfig.getWebService()), new HttpConfigImpl(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute()))
        );
    }

    private SocketTaskPoolExecutor createSocketTaskExecutor(ThreadPoolConfig threadPool, HttpProtocolHandler protocolHandler) {
        return new SocketTaskPoolExecutor(threadPool,
            new SocketTaskPool(threadPool,
                () -> new SocketTaskHandler(
                        protocolHandler
                )
            )
        );
    }
}
