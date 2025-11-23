package org.dochi.internal.http11;

import org.dochi.connector.Adapter;
import org.dochi.internal.AbstractHttpProcessor;
import org.dochi.internal.buffer.TmpBufferedOutputStream;
import org.dochi.net.AbstractSocketWrapper;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.net.AbstractEndpoint.Handler.SocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http11Processor extends AbstractHttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Http11InputBuffer inputBuffer;
    private final TmpBufferedOutputStream tempBufferOutputStream;

    public Http11Processor(Adapter adapter, HttpConfig config) {
        super(adapter);
        this.inputBuffer = new Http11InputBuffer(config.getReqConfig().getRequestHeaderMaxSize());
        this.request.setInputBuffer(this.inputBuffer);
        this.tempBufferOutputStream = new TmpBufferedOutputStream();
        this.response.setOutputStream(tempBufferOutputStream);
    }

    @Override
    public void recycle() {
        inputBuffer.recycle();
        tempBufferOutputStream.recycle();
        super.recycle();
    }

    protected boolean isKeepAlive(AbstractSocketWrapper<?> socketWrapper) {
        return isRequestKeepAlive() && isSeverKeepAlive(socketWrapper);
    }

    private boolean shouldKeepAlive(AbstractSocketWrapper<?> socketWrapper) {
        boolean isKeepAlive = isKeepAlive(socketWrapper);
        response.setConnection(isKeepAlive ? "keep-alive" : "close");
        if (isKeepAlive) {
            int timeout = socketWrapper.getKeepAliveTimeout();
            int maxRequests = socketWrapper.getMaxKeepAliveRequests();
            String keepAliveValue = "timeout=" + (timeout / 1000) + ", max=" + maxRequests;
            response.setKeepAlive(keepAliveValue);
        }
        return isKeepAlive;
    }

    private boolean isSeverKeepAlive(AbstractSocketWrapper<?> socketWrapper) {
        return !isReachedMax(socketWrapper.incrementKeepAliveCount(), socketWrapper.getMaxKeepAliveRequests());
    }

    private boolean isReachedMax(int currentCount, int maxCount) {
        return currentCount >= maxCount;
    }

    private boolean isRequestKeepAlive() {
        String connectionValue = this.request.headers().getHeader("connection");
        if (this.request.protocol().equalsIgnoreCase("HTTP/1.1")) {
            return !(connectionValue != null && connectionValue.equals("close"));
        }
        return this.request.protocol().equalsIgnoreCase("HTTP/1.0") && (connectionValue != null && connectionValue.equals("keep-alive"));
    }

    @Override
    protected void setSocketWrapper(AbstractSocketWrapper<?> socketWrapper) {
        inputBuffer.init(socketWrapper);
        tempBufferOutputStream.init(socketWrapper); // later -> outputBuffer.init(socketWrapper);
    }

    protected SocketState service(AbstractSocketWrapper<?> socketWrapper) throws Exception {
        boolean isKeepAlive = true;
        while (isKeepAlive) {
            socketWrapper.setConnectionTimeout(socketWrapper.getKeepAliveTimeout());

            if (!inputBuffer.parseHeader(request)) {
                // EOF -> read -1 -> false -> client close -> disconnection
                return SocketState.CLOSED;
            }

            if (isUpgradeRequest(socketWrapper)) {
                // Current ignore HTTP/1.1 upgrade request, processing as HTTP/1.1 (Later support HTTP/2.0)
                // 1. After client preface request
                    // (1) upgradeToken(); // upgradeToken = getHeader(Upgrade) & getHeader(HTTP2-Settings);
                    // (2) sendUpgrade(); // HTTP/1.1 response 101 Switching Protocols
                // 2. response as HTTP/2.0 using Http2Processor
                return SocketState.UPGRADING; // not need http api
            }

            if (!shouldKeepAlive(socketWrapper)) {
                isKeepAlive = false;
            }

            getAdapter().service(request, response);

            response.flush();

            this.recycle(); // low-level recycle
        }
        return SocketState.CLOSED;
    }

    private boolean isUpgradeRequest(AbstractSocketWrapper<?> socketWrapper) {
        String connectionValue = this.request.headers().getHeader("connection");
        String upgradeValue = this.request.headers().getHeader("upgrade");
        return connectionValue.equalsIgnoreCase("upgrade") &&
                upgradeValue.equalsIgnoreCase("h2c");
    }

//    private void sendUpgrade() {
//
//    }
}

