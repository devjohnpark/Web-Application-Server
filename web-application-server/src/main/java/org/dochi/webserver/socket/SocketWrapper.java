package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.KeepAlive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.net.SocketException;

// 어떤 Socket을 쓸지 모르기 때문에 호환성 지켜서 기능을 수정해야한다. ->
// Socket 객체를 변수로 받아서 스레드 재활용
public class SocketWrapper {
    private static final Logger log = LoggerFactory.getLogger(SocketWrapper.class);
    private Socket socket = null;
    private final int keepAliveTimeout;
    private final int maxKeepAliveRequests;

    // 스레드마다 서로 다른 객체를 사용하는 환경에서 Socket을 주입할때(setConnectedSocket) 초기화하므로, 공유 변수에 대한 메인 메모리 일관성 보장됨 (가시성을 위한 volatile 선언 필요없음)
    private int keepAliveCount = 0;
//    private SocketState socketState;

    public SocketWrapper(KeepAlive keepAlive) {
        this.keepAliveTimeout = keepAlive.getKeepAliveTimeout();
        this.maxKeepAliveRequests = keepAlive.getMaxKeepAliveRequests();
    }

    public void setConnectedSocket(Socket socket) {
        if (socket == null) {
            throw new IllegalArgumentException("Socket is null");
        }
        if (!isConnected(socket)) {
            throw new IllegalArgumentException("Socket is not connected");
        }
        this.socket = socket;
        this.keepAliveCount = 0;
    }

    public Socket getSocket() {
        if (socket == null) {
            throw new IllegalStateException("Socket is not set");
        }
        return socket;
    }

    private boolean isConnected(Socket socket) {
        return !socket.isClosed() && socket.isConnected();
    }

    public boolean isUsing() {
        return socket != null && !socket.isClosed();
    }

    public void startConnectionTimeout(int connectionTimeout) throws SocketException {
        getSocket().setSoTimeout(connectionTimeout);
        log.debug("Start connection timeout: {} [Client IP: {}]", connectionTimeout, getSocket().getInetAddress());
    }

    public int incrementKeepAliveCount() {
        return ++keepAliveCount;
    }

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }

}
