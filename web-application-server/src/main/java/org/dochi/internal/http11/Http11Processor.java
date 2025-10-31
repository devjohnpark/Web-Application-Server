package org.dochi.internal.http11;

import org.dochi.connector.InternalAdapter;
import org.dochi.internal.buffer.TmpBufferedOutputStream;
import org.dochi.internal.processor.AbstractHttpProcessor;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.socket.SocketWrapper;
import org.dochi.webserver.socket.SocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.dochi.webserver.socket.SocketState.*;

public class Http11Processor extends AbstractHttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Http11InputBuffer inputBuffer;
    private final TmpBufferedOutputStream tempBufferOutputStream;

    public Http11Processor(InternalAdapter mapper, HttpConfig config) {
        super(mapper, config);
        this.inputBuffer = new Http11InputBuffer(config.getHttpReqConfig().getRequestHeaderMaxSize());
        this.request.setInputBuffer(this.inputBuffer);
        this.tempBufferOutputStream = new TmpBufferedOutputStream();
        this.response.setOutputStream(tempBufferOutputStream);
    }

    @Override
    protected void recycle() {
        inputBuffer.recycle();
        tempBufferOutputStream.recycle();
        super.recycle();
    }

    protected boolean isKeepAlive(SocketWrapper<?> socketWrapper) {
        return isRequestKeepAlive() && isSeverKeepAlive(socketWrapper);
    }

    private boolean shouldKeepAlive(SocketWrapper<?> socketWrapper) {
        boolean isKeepAlive = isKeepAlive(socketWrapper);
        response.setConnection(isKeepAlive ? "keep-alive" : "close");
        if (isKeepAlive) {
            int timeout = socketWrapper.getConfigKeepAliveTimeout();
            int maxRequests = socketWrapper.getConfigMaxKeepAliveRequests();
            String keepAliveValue = "timeout=" + (timeout / 1000) + ", max=" + maxRequests;
            response.setKeepAlive(keepAliveValue);
        }
        return isKeepAlive;
    }

    private boolean isSeverKeepAlive(SocketWrapper<?> socketWrapper) {
        return !isReachedMax(socketWrapper.incrementKeepAliveCount(), socketWrapper.getConfigMaxKeepAliveRequests());
    }

    private boolean isReachedMax(int currentCount, int maxCount) {
        return currentCount >= maxCount;
    }

//    private boolean isRequestKeepAlive() {
//        String connectionValue = this.requestFacade.getHeader("connection");
//        if (this.requestFacade.getProtocol().equals("HTTP/1.1")) {
//            return !(connectionValue != null && connectionValue.equals("close"));
//        }
//        return this.requestFacade.getProtocol().equals("HTTP/1.0") && (connectionValue != null && connectionValue.equals("keep-alive"));
//    }

    private boolean isRequestKeepAlive() {
        String connectionValue = this.request.headers().getHeader("connection");
        if (this.request.protocol().equalsIgnoreCase("HTTP/1.1")) {
            return !(connectionValue != null && connectionValue.equals("close"));
        }
        return this.request.protocol().equalsIgnoreCase("HTTP/1.0") && (connectionValue != null && connectionValue.equals("keep-alive"));
    }

    @Override
    protected void setSocketWrapper(SocketWrapper<?> socketWrapper) {
        inputBuffer.init(socketWrapper);
        tempBufferOutputStream.init(socketWrapper); // later -> outputBuffer.init(socketWrapper);
    }

    protected SocketState service(SocketWrapper<?> socketWrapper) throws IOException {
        boolean isKeepAlive = true;
        while (isKeepAlive) {
            socketWrapper.setConnectionTimeout(socketWrapper.getConfigKeepAliveTimeout());

            if (!inputBuffer.parseHeader(request)) {
                // EOF -> read -1 -> false -> client close -> disconnection
                return CLOSED;
            }

            if (isUpgradeRequest(socketWrapper)) {
                // Current ignore HTTP/1.1 upgrade request, processing as HTTP/1.1 (Later support HTTP/2.0)
                // 1. After client preface request
                    // (1) upgradeToken(); // upgradeToken = getHeader(Upgrade) & getHeader(HTTP2-Settings);
                    // (2) sendUpgrade(); // HTTP/1.1 response 101 Switching Protocols
                // 2. response as HTTP/2.0 using Http2Processor
                return UPGRADING; // not need http api
            }

            if (!shouldKeepAlive(socketWrapper)) {
                isKeepAlive = false;
            }

            getDispatcher().service(request, response);

            response.flush();



            // Response object provides OutputStream object to developer, so it need flush() after processing HTTP API
            // flush() has system call cost, it needs to remove inefficient action.
            // 1. Rapping flush method by custom OutputStream.
            // 2. The custom OutputStream declares boolean-isFlushed variable.
            // 3. If call rapped flush method, According to isFlushed value(true/false), flush() to be called or not.
            this.recycle(); // low-level recycle
        }
        return CLOSED;
    }

    private boolean isUpgradeRequest(SocketWrapper<?> socketWrapper) {
        String connectionValue = this.request.headers().getHeader("connection");
        String upgradeValue = this.request.headers().getHeader("upgrade");
        return connectionValue.equalsIgnoreCase("upgrade") &&
                upgradeValue.equalsIgnoreCase("h2c");
    }

//    private void sendUpgrade() {
//
//    }
}

