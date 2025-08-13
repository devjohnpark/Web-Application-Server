//package org.dochi.webserver;
//
//import org.dochi.webserver.attribute.KeepAlive;
//import org.dochi.webserver.config.ServerConfig;
//import org.dochi.http.api.HttpApiMapper;
//import org.dochi.webserver.socket.SocketTaskHandler;
//import org.dochi.webserver.socket.SocketTaskPool;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class SocketTaskHandlerPoolTest {
//    private SocketTaskPool socketTaskPool;
//    private final int poolSize = 10;
//    private final KeepAlive keepAlive = new KeepAlive();
//    private final HttpApiMapper httpApiMapper = new HttpApiMapper(new ServerConfig().getWebService().getServices());
//
//    @BeforeEach
//    void setUp() {
//        socketTaskPool = new SocketTaskPool(poolSize, keepAlive, httpApiMapper);
//    }
//
//    @Test
//    void getAvailableRequestHandler() {
//        for (int i = 0; i < poolSize; i++) {
//            socketTaskPool.getAvailableRequestHandler(keepAlive, httpApiMapper);
//        }
//        assertNotNull(socketTaskPool.getAvailableRequestHandler(keepAlive, httpApiMapper));
//    }
//
//    @Test
//    void recycleRequestHandler() {
//        int currentPoolSize = socketTaskPool.getPoolSize();
//        SocketTaskHandler requestTaskHandler = socketTaskPool.getAvailableRequestHandler(keepAlive, httpApiMapper);
//        socketTaskPool.recycle(requestTaskHandler);
//        assertEquals(currentPoolSize, socketTaskPool.getPoolSize());
//    }
//}