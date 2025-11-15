package org.dochi.webserver.bootstrap;

import org.dochi.webserver.lifecycle.AbstractLifecycle;
import org.dochi.webserver.lifecycle.LifecycleException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;

class WebAppServerTest {
    private WebAppServer was;

    private int availablePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    // 동일 포트와 동일 호스트 네임일 경우, start() 호출시 LifecyleException 발생하고 나머지 경우는 모두 예외 안발생
    @Test
    void start_samePortAndHostname_throwsLifecycleException() throws Exception {
        int port = availablePort();
        WebAppServer server1 = new WebAppServer(port, "localhost");
        WebAppServer server2 = new WebAppServer(port, "localhost");

        server1.start();

        assertThrows(LifecycleException.class, server2::start);
    }

    @Test
    void start_differentPortSameHostname_noException() throws Exception {
        int port1 = availablePort();
        int port2 = availablePort();
        WebAppServer server1 = new WebAppServer(port1, "localhost");
        WebAppServer server2 = new WebAppServer(port2, "localhost");

        assertDoesNotThrow(() -> {
            server1.start();
            server2.start();
        });
    }

    @Test
    void start_samePortDifferentHostname_noException() throws Exception {
        int port = availablePort();
        WebAppServer server1 = new WebAppServer(port, "localhost");
        WebAppServer server2 = new WebAppServer(port, "0.0.0.0");

        assertDoesNotThrow(() -> {
            server1.start();
            server2.start();
        });
    }

    @Test
    void start_differentPortAndHostname_noException() throws Exception {
        int port1 = availablePort();
        int port2 = availablePort();
        WebAppServer server1 = new WebAppServer(port1, "localhost");
        WebAppServer server2 = new WebAppServer(port2, "0.0.0.0");

        assertDoesNotThrow(() -> {
            server1.start();
            server2.start();
        });
    }

    @Test
    void start() throws Exception {
        WebAppServer server = new WebAppServer(availablePort());
        server.start();
        assertEquals(server.getServer().getState(), AbstractLifecycle.State.STARTED);
    }

    @Test
    void stop() throws Exception {
        WebAppServer server = new WebAppServer(availablePort());
        server.stop();
        assertEquals(server.getServer().getState(), AbstractLifecycle.State.NEW);
    }

    @Test
    void start_and_stop() throws Exception {
        WebAppServer server = new WebAppServer(availablePort());
        server.start();
        server.stop();
        assertEquals(server.getServer().getState(), AbstractLifecycle.State.DESTROYED);
    }
}