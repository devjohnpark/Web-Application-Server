package org.dochi.net;

import org.dochi.webserver.config.SocketConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractSocketWrapper<S> {
    private static final Logger log = LoggerFactory.getLogger(AbstractSocketWrapper.class);
    protected final S socket;
    private final SocketConfig config;
    private int keepAliveCount = 0;

    protected AbstractSocketWrapper(S socket, SocketConfig config) {
        this.socket = socket;
        this.config = config;
    }

    public abstract int read(byte[] buffer, int off, int len) throws IOException;

    public abstract void write(byte[] buffer, int off, int len) throws IOException;

    public abstract void close();

    public abstract void flush() throws IOException;

    public abstract boolean isConnected();

    public abstract boolean isClosed();

    public int incrementKeepAliveCount() { return ++keepAliveCount; }

    public int getKeepAliveCount() { return keepAliveCount; }

    public abstract void setConnectionTimeout(int connectionTimeout) throws IOException;

    public int getKeepAliveTimeout() { return config.getKeepAliveTimeout(); }

    public int getMaxKeepAliveRequests() { return config.getMaxKeepAliveRequests(); }

    public int getConnectionTimeout() { return config.getConnectionTimeout(); }

    public abstract void setReceiveBufferSize(int receiveBufferSize) throws IOException;
    public abstract void setSendBufferSize(int sendBufferSize) throws IOException;

}
