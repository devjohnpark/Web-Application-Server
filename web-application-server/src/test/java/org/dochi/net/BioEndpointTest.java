package org.dochi.net;

import org.dochi.webserver.lifecycle.LifecycleException;
import org.junit.jupiter.api.*;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class BioEndpointTest {
    private BioEndpoint endpoint;

    private static class TestHandler implements AbstractEndpoint.Handler<Socket> {
        private final SocketState state = AbstractEndpoint.Handler.SocketState.OPEN;

        @Override
        public SocketState process(AbstractSocketWrapper<Socket> socket) {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
    }

    @BeforeEach
    void setUp() throws LifecycleException {
        endpoint = new BioEndpoint(0, "localhost");
        endpoint.setHandler(new TestHandler());
        endpoint.start();
    }

    @AfterEach
    void tearDown() throws LifecycleException {
        endpoint.stop();
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
        AbstractSocketWrapper<Socket> wrapper = endpoint.wrapSocket(socket);
        assertInstanceOf(BioSocketWrapper.class, wrapper);
    }

    @Test
    void wrapSocket_returnsNonNull() {
        AbstractSocketWrapper<Socket> wrapper = endpoint.wrapSocket(new Socket());
        assertNotNull(wrapper);
    }

    // createSocketTask() 검증
    @Test
    void createSocketTask_returnsNonNull() {
        AbstractSocketWrapper<Socket> wrapper = endpoint.wrapSocket(new Socket());

        AbstractSocketTask<Socket> task = endpoint.createSocketTask(wrapper);

        assertNotNull(task);
    }

    // processSocket() 검증
    @Test
    void processSocket_returnsTrue() {
        boolean result = endpoint.processSocketTask(new Socket());
        assertTrue(result);
    }


    @Test
    void processSocket_handlerClosed_taskReturnedToCache() throws InterruptedException {

        endpoint.setExecutor(Runnable::run);

        endpoint.processSocketTask(new Socket());

        assertFalse(endpoint.socketTaskPool.isEmpty());
    }

    @Test
    void processSocket_calledTwice_reusesCachedTask() throws InterruptedException {

        endpoint.setExecutor(Runnable::run);

        endpoint.processSocketTask(new Socket());

        int sizeAfterFirst = endpoint.socketTaskPool.size();

        endpoint.processSocketTask(new Socket());

        int sizeAfterSecond = endpoint.socketTaskPool.size();

        assertEquals(1, sizeAfterFirst);
        assertEquals(1, sizeAfterSecond);
    }
}