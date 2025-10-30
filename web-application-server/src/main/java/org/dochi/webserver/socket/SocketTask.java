package org.dochi.webserver.socket;

public interface SocketTask extends Runnable {
    SocketWrapper<?> getSocketWrapper();
    void setSocketWrapper(SocketWrapper<?> socketWrapper);
}
