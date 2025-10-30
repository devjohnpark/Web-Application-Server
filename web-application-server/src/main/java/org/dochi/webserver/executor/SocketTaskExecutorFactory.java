package org.dochi.webserver.executor;

import org.dochi.internal.mapper.HttpMapper;
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

    public SocketTaskPoolExecutor createExecutor(ServerConfig serverConfig) {
        return createSocketTaskExecutor(
            serverConfig.getThreadPool(),
            new HttpProtocolHandler(new HttpMapper(serverConfig.getWebService()), new HttpConfigImpl(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute()))
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
