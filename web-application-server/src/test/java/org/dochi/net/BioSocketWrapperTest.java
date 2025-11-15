package org.dochi.net;

import org.dochi.webserver.connect.Connector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class BioSocketWrapperTest {
    private static final Logger log = LoggerFactory.getLogger(BioSocketWrapperTest.class);

    private final Connector connector = new Connector();
    private final byte[] clientBuffer = new byte[] { 10, 20, 30, 40, 50 };
    private final byte[] serverBuffer = new byte[clientBuffer.length];

    @BeforeEach
    public void setUp() throws IOException {
        connector.connect();
    }

    @AfterEach
    public void tearDown() throws IOException {
        connector.disconnect();
    }

    @Test
    void server_connected_socket_read() throws IOException {
        connector.getClientConnectedSocket().write(clientBuffer, 0, clientBuffer.length);
        connector.getServerConnectedSocket().read(serverBuffer, 0, serverBuffer.length);
        assertArrayEquals(clientBuffer, serverBuffer);
    }

    @Test
    void server_connected_socket__write() throws IOException {
        connector.getServerConnectedSocket().write(serverBuffer, 0, serverBuffer.length);
        connector.getClientConnectedSocket().read(clientBuffer, 0, clientBuffer.length);
        assertArrayEquals(clientBuffer, serverBuffer);
    }

    @Test
    void server_connected_socket__close() throws IOException {
        connector.getServerConnectedSocket().close();
        assertThrows(SocketException.class, () -> connector.getServerConnectedSocket().read(clientBuffer, 0, clientBuffer.length));
    }

    @Test
    void isConnected() {
        assertTrue(connector.getServerConnectedSocket().isConnected());
    }

    @Test
    void isClosed() throws IOException {
        assertFalse(connector.getServerConnectedSocket().isClosed());
        connector.getServerConnectedSocket().close();
        assertTrue(connector.getServerConnectedSocket().isClosed());
    }

    @Test
    void startConnectionTimeout_after_close() throws IOException, InterruptedException {
        connector.getServerConnectedSocket().close();
        assertThrows(SocketException.class, () -> connector.getServerConnectedSocket().setConnectionTimeout(connector.getServerConnectedSocket().getKeepAliveTimeout()));
    }

    @Test
    void startConnectionTimeout_socketTimeout() throws IOException, InterruptedException {
        int connectionTimeout = 1000;
        connector.getServerConnectedSocket().setConnectionTimeout(connectionTimeout);
        Thread readThread = new Thread(() -> {
            // SocketTimeoutException은 read가 blocking 상태일때 발생, 따라서 서버의 연결 소켓 read() 후에 connector timeout 시간이 지난뒤 클랑이언트 연결 소켓 write()
            assertThrows(SocketTimeoutException.class, () -> connector.getServerConnectedSocket().read(serverBuffer, 0, serverBuffer.length));
        });
        readThread.start();
        Thread.sleep(connectionTimeout); // 충분한 시간 대기하여 타임아웃 발생 유도
        connector.getClientConnectedSocket().write(clientBuffer, 0, clientBuffer.length); // 이후에 데이터 전송
    }
}