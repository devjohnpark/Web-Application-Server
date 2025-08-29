package org.dochi.webserver.socket;

import org.dochi.api.mapper.HttpApiMapper;
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
            new HttpProtocolHandler(new HttpConfigImpl(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute())),
            new HttpApiMapper(serverConfig.getWebService())
        );
    }

    private SocketTaskExecutor createSocketTaskExecutor(ThreadPoolConfig threadPool, HttpProtocolHandler protocolHandler, HttpApiMapper httpApiMapper) {
        return new SocketTaskExecutor(threadPool,
            new SocketTaskPool(threadPool,
                () -> new SocketTaskHandler(
                        protocolHandler,
                        httpApiMapper
                )
            )
        );
    }
}
