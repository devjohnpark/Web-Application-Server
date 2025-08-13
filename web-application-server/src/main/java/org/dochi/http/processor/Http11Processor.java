package org.dochi.http.processor;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.request.data.RequestHeaders;
import org.dochi.http.request.processor.Http11RequestProcessor;
import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.request.stream.Http11RequestStream;
import org.dochi.http.response.processor.Http11ResponseProcessor;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.socket.SocketState;
import org.dochi.webserver.socket.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.dochi.webserver.socket.SocketState.*;

public class Http11Processor extends AbstractHttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    public Http11Processor(InputStream in, OutputStream out, HttpConfig config) {
        super(
                new Http11RequestProcessor(in, config.getHttpReqConfig()),
                new Http11ResponseProcessor(out, config.getHttpResConfig())
        );
    }

    public boolean shouldPersistentConnection(SocketWrapper socketWrapper) {
        return isRequestKeepAlive() && isSeverKeepAlive(socketWrapper);
    }

    private boolean shouldNext(SocketWrapper socketWrapper) {
        boolean isKeepAlive = shouldPersistentConnection(socketWrapper);
        response.addConnection(isKeepAlive);
        if (isKeepAlive) {
           response.addKeepAlive(socketWrapper.getKeepAliveTimeout(), socketWrapper.getMaxKeepAliveRequests());
        }
        return isKeepAlive;
    }

    private boolean isSeverKeepAlive(SocketWrapper socketWrapper) {
        return !isReachedMax(socketWrapper.incrementKeepAliveCount(), socketWrapper.getMaxKeepAliveRequests());
    }

    private static boolean isReachedMax(int currentCount, int maxCount) {
        return currentCount >= maxCount;
    }

    private boolean isRequestKeepAlive() {
        String connectionValue = request.getConnection();
        if (request.getHttpVersion().equals(HttpVersion.HTTP_1_1)) {
            return !(connectionValue != null && connectionValue.equals("close"));
        }
        return request.getHttpVersion().equals(HttpVersion.HTTP_1_0) && (connectionValue != null && connectionValue.equals("keep-alive"));
    }

    protected SocketState service(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper) throws IOException {
        SocketState state = OPEN;
        int processCount = 0;
        while (state == OPEN) {
            if (!request.isPrepareHeader()) {
                request.recycle();
                state = CLOSED;
                break;
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
            httpApiMapper.getHttpApiHandler(request.getPath()).service(request, response);
            response.flush();
            // response.flush()
            // Response object provides OutputStream object to developer, so it need flush() after processing HTTP API
            // flush() has system call cost, it needs to remove inefficient action.
            // 1. Rapping flush method by custom OutputStream.
            // 2. The custom OutputStream declares boolean-isFlushed variable.
            // 3. If call rapped flush method, According to isFlushed value(true/false), flush() to be called or not.
            processCount++;
            recycle();
        }
        log.debug("Processed requests count: {}", processCount);
        return state;
    }

    private boolean isUpgradeRequest(SocketWrapper socketWrapper) {
        return request.getHeader(RequestHeaders.UPGRADE) != null;
    }

//    private void sendUpgrade() {
//
//    }
}

