package org.dochi.webserver.socket;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.webserver.config.*;
import org.dochi.webserver.protocol.HttpProtocolHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SocketTaskPoolTest {
    SocketTaskPool socketTaskPool;
    ServerConfig serverConfig = new ServerConfig();
    HttpConfig httpConfig = new HttpReqResConfig(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute());
    HttpProtocolHandler protocolHandler = new HttpProtocolHandler(httpConfig, serverConfig.getHttpProcessorAttribute());
    HttpApiMapper apiMapper = new HttpApiMapper(serverConfig.getWebService());

    @BeforeEach
    void setUp() {
        socketTaskPool = new SocketTaskPool(serverConfig.getThreadPool(), () -> new SocketTaskHandler(protocolHandler, apiMapper));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void recycle() {
        int poolSize = socketTaskPool.getPoolSize();
        SocketTask socketTaskHandler = socketTaskPool.get();
        assertEquals(poolSize - 1, socketTaskPool.getPoolSize());
        socketTaskPool.recycle(socketTaskHandler);
        assertEquals(poolSize, socketTaskPool.getPoolSize());
    }

    @Test
    void get() {
        int poolSize = socketTaskPool.getPoolSize();
        socketTaskPool.get();
        assertEquals(poolSize - 1, socketTaskPool.getPoolSize());
    }
}