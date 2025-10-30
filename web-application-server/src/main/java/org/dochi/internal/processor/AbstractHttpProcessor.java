package org.dochi.internal.processor;

import org.dochi.connector.*;
import org.dochi.http.utils.HttpStatus;
import org.dochi.internal.mapper.HttpMapper;
import org.dochi.connector.RequestFacade;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.socket.SocketWrapper;
import org.dochi.webserver.socket.SocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static org.dochi.webserver.socket.SocketState.CLOSED;

public abstract class AbstractHttpProcessor implements HttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpProcessor.class);
    protected final RequestFacade requestFacade;
    protected final Http11ResponseFacade responseFacade;
    protected final HttpMapper mapper;

    protected AbstractHttpProcessor(HttpMapper mapper, HttpConfig config) {
        this.mapper = mapper;
        this.requestFacade = new RequestFacade(config.getHttpReqConfig());
        this.responseFacade = new Http11ResponseFacade(config.getHttpResConfig());
    }

    @Override
    public SocketState process(SocketWrapper<?> socketWrapper) {
        setSocketWrapper(socketWrapper);
        try {
            recycle();
            return service(socketWrapper);
        } catch (Exception e) {
            resolveException(e);
        } finally {
            log.info("Process count: {}", socketWrapper.getKeepAliveCount());
        }
        return CLOSED;
    }

    protected void recycle() {
        requestFacade.recycle();
        responseFacade.recycle();
    }

    protected HttpMapper getHttpMapper() {
        return mapper;
    }

    abstract protected void setSocketWrapper(SocketWrapper<?> socketWrapper);

    protected abstract SocketState service(SocketWrapper<?> socketWrapper) throws IOException;

    protected abstract boolean isKeepAlive(SocketWrapper<?> socketWrapper);
    
    private void resolveException(Exception e) {
        switch (e) {
            case IllegalArgumentException illegalArgumentException -> { // wrong input from client
                // WAS internal input or parsing exception, I decide close connection
                sendClosedError(HttpStatus.BAD_REQUEST, e.getMessage());
            } case SocketTimeoutException socketTimeoutException -> {
                // SocketTimeoutException exception thrown when valid time expires while being blocked by read() method of SocketInputStream object (write() is not related to setSoTimeout)
                // 408 must be closed
                sendClosedError(HttpStatus.REQUEST_TIMEOUT, e.getMessage());
            } case SocketException socketException -> {
                // reference: NioSocketImpl.implRead()
                //  If call Socket.read() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Connection reset") internally in Socket
                //  If call Socket.write() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Socket closed") internally in Socket
                log.error("Socket was read or write after the client closed connection: ", e);
            } case null, default -> { // IOException, RuntimeException, Exception
                assert e != null;
                // All other exceptions as internal server error then close
                sendClosedError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    private void sendClosedError(HttpStatus status, String errorMessage) {
        log.error("HTTP status: {} {}, Reason: {}", String.valueOf(status.getCode()), status.getMessage(), errorMessage);
        try {
            responseFacade.setConnection("close"); // 408, 500 close 
            responseFacade.sendError(status, errorMessage);
            responseFacade.flush();
        } catch (IOException e) {
            log.error("Failed to send error response: {}", e.getMessage());
        }
    }
}
