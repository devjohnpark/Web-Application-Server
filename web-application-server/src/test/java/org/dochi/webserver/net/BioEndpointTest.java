package org.dochi.webserver.net;

import org.dochi.webserver.lifecycle.LifecycleException;
import org.junit.jupiter.api.*;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BioEndpointTest {
    private BioEndpoint endpoint;

    private static class TestHandler implements EndpointBase.Handler<Socket> {
        private EndpointBase.Handler.SocketState state = EndpointBase.Handler.SocketState.OPEN;

        @Override
        public EndpointBase.Handler.SocketState process(SocketWrapperBase<Socket> socket) {
            return EndpointBase.Handler.SocketState.OPEN;
        }
    }

    @BeforeEach
    void setUp() throws LifecycleException {
        endpoint = new BioEndpoint(8080, "localhost");
        endpoint.init();
        endpoint.start();
    }

    @AfterEach
    void tearDown() throws LifecycleException {
        endpoint.stop();
        endpoint.destroy();
    }


    // setExecutor, setHandler 유효성 검증
    @Test
    void setExecutor_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> endpoint.setExecutor(null));
    }

    @Test
    void setHandler_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> endpoint.setHandler(null));
    }

    // wrapSocket() 검증
    @Test
    void wrapSocket_returnsBioSocketWrapper() {
        Socket socket = new Socket();
        SocketWrapperBase<Socket> wrapper = endpoint.wrapSocket(socket);
        assertInstanceOf(BioSocketWrapper.class, wrapper);
    }

    @Test
    void wrapSocket_returnsNonNull() {
        SocketWrapperBase<Socket> wrapper = endpoint.wrapSocket(new Socket());
        assertNotNull(wrapper);
    }

    // createSocketTask() 검증
    @Test
    void createSocketTask_returnsNonNull() {
        endpoint.setHandler(new TestHandler());
        SocketWrapperBase<Socket> wrapper = endpoint.wrapSocket(new Socket());

        SocketTaskBase<Socket> task = endpoint.createSocketTask(wrapper);

        assertNotNull(task);
    }

    // processSocket() 검증
    @Test
    void processSocket_returnsTrue() {
        TestHandler handler = new TestHandler();
        endpoint.setHandler(handler);
        boolean result = endpoint.processSocket(new Socket());
        assertTrue(result);
    }


    @Test
    void processSocket_handlerClosed_taskReturnedToCache() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        endpoint.setHandler(wrapper -> {
            latch.countDown();
            return EndpointBase.Handler.SocketState.CLOSED;
        });

        endpoint.processSocket(new Socket());
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));

        assertFalse(endpoint.socketTaskCache.isEmpty());
    }

    @Test
    void processSocket_taskReturnedToCache() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        endpoint.setHandler(wrapper -> {
            latch.countDown();
            return EndpointBase.Handler.SocketState.OPEN;
        });

        endpoint.processSocket(new Socket());
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));

        assertFalse(endpoint.socketTaskCache.isEmpty());
    }

    @Test
    void processSocket_calledTwice_reusesCachedTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        endpoint.setHandler(wrapper -> {
            latch.countDown();
            return EndpointBase.Handler.SocketState.OPEN;
        });

        endpoint.processSocket(new Socket());
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));

        int sizeAfterFirst = endpoint.socketTaskCache.size();

        CountDownLatch latch2 = new CountDownLatch(1);
        endpoint.setHandler(wrapper -> {
            latch2.countDown();
            return EndpointBase.Handler.SocketState.OPEN;
        });

        endpoint.processSocket(new Socket());
        assertTrue(latch2.await(100, TimeUnit.MILLISECONDS));

        int sizeAfterSecond = endpoint.socketTaskCache.size();

        assertEquals(1, sizeAfterFirst);
        assertEquals(1, sizeAfterSecond);
    }
}