package org.dochi.webserver.socket;

import org.dochi.webserver.connect.Connection;
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

    private final Connection connection = new Connection();
    private final byte[] clientBuffer = new byte[] { 10, 20, 30, 40, 50 };
    private final byte[] serverBuffer = new byte[clientBuffer.length];

    @BeforeEach
    public void setUp() throws IOException {
        connection.connect();
    }

    @AfterEach
    public void tearDown() throws IOException {
        connection.disconnect();
    }

    @Test
    void server_connected_socket_read() throws IOException {
        connection.getClientConnectedSocket().write(clientBuffer, 0, clientBuffer.length);
        connection.getServerConnectedSocket().read(serverBuffer, 0, serverBuffer.length);
        assertArrayEquals(clientBuffer, serverBuffer);
    }

    @Test
    void server_connected_socket__write() throws IOException {
        connection.getServerConnectedSocket().write(serverBuffer, 0, serverBuffer.length);
        connection.getClientConnectedSocket().read(clientBuffer, 0, clientBuffer.length);
        assertArrayEquals(clientBuffer, serverBuffer);
    }

    @Test
    void server_connected_socket__close() throws IOException {
        connection.getServerConnectedSocket().close();
        assertThrows(SocketException.class, () -> connection.getServerConnectedSocket().read(clientBuffer, 0, clientBuffer.length));
    }

    @Test
    void isConnected() {
        assertTrue(connection.getServerConnectedSocket().isConnected());
    }

    @Test
    void isClosed() throws IOException {
        assertFalse(connection.getServerConnectedSocket().isClosed());
        connection.getServerConnectedSocket().close();
        assertTrue(connection.getServerConnectedSocket().isClosed());
    }

    @Test
    void startConnectionTimeout_after_close() throws IOException, InterruptedException {
        connection.getServerConnectedSocket().close();
        assertThrows(SocketException.class, () -> connection.getServerConnectedSocket().setConnectionTimeout(connection.getServerConnectedSocket().getConfigKeepAliveTimeout()));
    }

    @Test
    void startConnectionTimeout_socketTimeout() throws IOException, InterruptedException {
        int connectionTimeout = 1000;
        connection.getServerConnectedSocket().setConnectionTimeout(connectionTimeout);
        Thread readThread = new Thread(() -> {
            // SocketTimeoutException은 read가 blocking 상태일때 발생, 따라서 서버의 연결 소켓 read() 후에 connection timeout 시간이 지난뒤 클랑이언트 연결 소켓 write()
            assertThrows(SocketTimeoutException.class, () -> connection.getServerConnectedSocket().read(serverBuffer, 0, serverBuffer.length));
        });
        readThread.start();
        Thread.sleep(connectionTimeout); // 충분한 시간 대기하여 타임아웃 발생 유도
        connection.getClientConnectedSocket().write(clientBuffer, 0, clientBuffer.length); // 이후에 데이터 전송
    }
}