package org.dochi.internal.processor;

import org.dochi.http.utils.ResponseHeaders;
import org.dochi.internal.http11.Http11InputBuffer;
import org.dochi.internal.mapper.HttpMapper;
import org.dochi.connector.TmpBufferedOutputStream;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.socket.SocketWrapperBase;
import org.dochi.webserver.socket.SocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.dochi.webserver.socket.SocketState.*;

public class Http11Processor extends AbstractHttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Http11InputBuffer inputBuffer;
    private final TmpBufferedOutputStream tempBufferOutputStream;

    public Http11Processor(HttpMapper mapper, HttpConfig config) {
        super(mapper, config);
        this.inputBuffer = new Http11InputBuffer(config.getHttpReqConfig().getRequestHeaderMaxSize());
        this.requestHandler.setInputBuffer(this.inputBuffer);
        this.tempBufferOutputStream = new TmpBufferedOutputStream();
        this.responseHandler.setOutputStream(this.tempBufferOutputStream);
    }

    @Override
    protected void recycle() {
        inputBuffer.recycle();
        tempBufferOutputStream.recycle();
        super.recycleHandler();
    }

    protected boolean shouldKeepAlive(SocketWrapperBase<?> socketWrapper) {
        return isRequestKeepAlive() && isSeverKeepAlive(socketWrapper);
    }

    private boolean shouldNext(SocketWrapperBase<?> socketWrapper) {
        boolean isKeepAlive = shouldKeepAlive(socketWrapper);
        responseHandler.addConnection(isKeepAlive);
        if (isKeepAlive) {
            int timeout = socketWrapper.getConfigKeepAliveTimeout();
            int maxRequests = socketWrapper.getConfigMaxKeepAliveRequests();

            StringBuilder keepAlive = new StringBuilder();

            if (timeout > 0) {
                keepAlive.append("timeout=").append(timeout / 1000);
            }

            if (maxRequests > 0) {
                if (!keepAlive.isEmpty()) {
                    keepAlive.append(", ");
                }
                keepAlive.append("max=").append(maxRequests);
            }
            responseHandler.addHeader(ResponseHeaders.KEEP_ALIVE, keepAlive.toString());
        }
        return isKeepAlive;
    }

    private boolean isSeverKeepAlive(SocketWrapperBase<?> socketWrapper) {
        return !isReachedMax(socketWrapper.incrementKeepAliveCount(), socketWrapper.getConfigMaxKeepAliveRequests());
    }

    private boolean isReachedMax(int currentCount, int maxCount) {
        return currentCount >= maxCount;
    }

    private boolean isRequestKeepAlive() {
        String connectionValue = this.requestHandler.getHeader("connection");
        if (this.requestHandler.getProtocol().equals("HTTP/1.1")) {
            return !(connectionValue != null && connectionValue.equals("close"));
        }
        return this.requestHandler.getProtocol().equals("HTTP/1.0") && (connectionValue != null && connectionValue.equals("keep-alive"));
    }

    @Override
    protected void setSocketWrapper(SocketWrapperBase<?> socketWrapper) {
        inputBuffer.init(socketWrapper);
        tempBufferOutputStream.init(socketWrapper); // later -> outputBuffer.init(socketWrapper);
    }

    protected SocketState service(SocketWrapperBase<?> socketWrapper) throws IOException {
        SocketState state = OPEN;
        while (state == OPEN) {
            if (!inputBuffer.parseHeader(requestHandler.getRequest())) {
                // request line null -> false -> disconnection
                return CLOSED;
            } else if (isUpgradeRequest(socketWrapper)) {
                // Current ignore HTTP/1.1 upgrade request, processing as HTTP/1.1 (Later support HTTP/2.0)
                state = UPGRADING;
                // 1. upgradeToken(); // upgradeToken = getHeader(Upgrade) & getHeader(HTTP2-Settings);
                // 2. sendUpgrade(); // HTTP/1.1 response 101 status
                // 3. break;
                // After client preface request -> response as HTTP/2.0 using Http2Processor
            } else if (!shouldNext(socketWrapper)) {
                state = CLOSED;
            }
            getHttpMapper().getHttpApiHandler(requestHandler.getPath()).service(requestHandler, responseHandler);
            responseHandler.flush();
            // Response object provides OutputStream object to developer, so it need flush() after processing HTTP API
            // flush() has system call cost, it needs to remove inefficient action.
            // 1. Rapping flush method by custom OutputStream.
            // 2. The custom OutputStream declares boolean-isFlushed variable.
            // 3. If call rapped flush method, According to isFlushed value(true/false), flush() to be called or not.
            recycle();
            resetKeepAliveTimeout(socketWrapper, state);
        }
        return state;
    }

    private void resetKeepAliveTimeout(SocketWrapperBase<?> socketWrapper, SocketState state) throws IOException {
        if (state == OPEN) {
            socketWrapper.setConnectionTimeout(socketWrapper.getConfigKeepAliveTimeout());
        }
    }

    private boolean isUpgradeRequest(SocketWrapperBase<?> socketWrapper) {
        return requestHandler.getHeader("upgrade") != null;
    }

    //    private void sendUpgrade() {
//
//    }
}

