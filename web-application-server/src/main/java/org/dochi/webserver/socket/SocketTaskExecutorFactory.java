package org.dochi.webserver.socket;

import org.dochi.internal.mapper.HttpMapper;
import org.dochi.webserver.protocol.HttpProtocolHandler;
import org.dochi.webserver.config.*;

public class SocketTaskExecutorFactory {
    private static final SocketTaskExecutorFactory INSTANCE = new SocketTaskExecutorFactory();

    private SocketTaskExecutorFactory() {}

    public static SocketTaskExecutorFactory getInstance() {
        return INSTANCE;
    }

    public SocketTaskExecutor createExecutor(ServerConfig serverConfig) {
        return createSocketTaskExecutor(
            serverConfig.getThreadPool(),
            new HttpProtocolHandler(new HttpMapper(serverConfig.getWebService()), new HttpConfigImpl(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute()))
        );
    }

    private SocketTaskExecutor createSocketTaskExecutor(ThreadPoolConfig threadPool, HttpProtocolHandler protocolHandler) {
        return new SocketTaskExecutor(threadPool,
            new SocketTaskPool(threadPool,
                () -> new SocketTaskHandler(
                        protocolHandler
                )
            )
        );
    }
}
