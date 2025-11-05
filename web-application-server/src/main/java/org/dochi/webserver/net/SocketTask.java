package org.dochi.webserver.net;

public interface SocketTask extends Runnable {
    SocketWrapperBase<?> getSocketWrapper();
    void setSocketWrapper(SocketWrapperBase<?> socketWrapper);
}
