package org.dochi.internal.processor;

import org.dochi.connector.*;
import org.dochi.http.utils.HttpStatus;
import org.dochi.internal.mapper.HttpMapper;
import org.dochi.connector.RequestFacade;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.socket.SocketWrapperBase;
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
    public SocketState process(SocketWrapperBase<?> socketWrapper) {
        if (socketWrapper == null) {
            throw new IllegalArgumentException("SocketWrapperBase is null");
        }
        SocketState state = CLOSED;
        setSocketWrapper(socketWrapper);
        try {
            recycle();
            state = service(socketWrapper);
        } catch (Exception e) {
            processException(e);
            recycle(); // shouldn't call when upgrading protocol
        } finally {
            log.info("Process count: {}", socketWrapper.getKeepAliveCount());
        }
        return state;
    }

    abstract protected void recycle();

    protected void recycleFacade() {
        requestFacade.recycle();
        responseFacade.recycle();
    }

    protected HttpMapper getHttpMapper() {
        return mapper;
    }

    abstract protected void setSocketWrapper(SocketWrapperBase<?> socketWrapper);

    protected abstract SocketState service(SocketWrapperBase<?> socketWrapper) throws IOException;

    protected abstract boolean shouldKeepAlive(SocketWrapperBase<?> socketWrapper);

    // Because the developer has the option to handle RuntimeException, RuntimeException propagated by not catching it is considered to be an invalid request from the client and a 400 response is sent.
    // Unexpected IOException on input/output, 500 response because Exception is a server problem.
    private void processException(Exception e) {
        switch (e) {
            case SocketTimeoutException socketTimeoutException -> {
//                SocketTimeoutException exception thrown when valid time expires while being blocked by read() method of SocketInputStream object (write() is not related to setSoTimeout)
                sendError(HttpStatus.REQUEST_TIMEOUT, e.getMessage());
            }
            case SocketException socketException -> {
                // reference: NioSocketImpl.implRead()
                //  If call Socket.read() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Connection reset") internally in Socket
                //  If call Socket.write() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Socket closed") internally in Socket
                log.error("Socket was read or write after the client closed connection: ", e);
            }
            case RuntimeException runtimeException -> {
                sendError(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            case null, default -> { // IOException, Exception
                assert e != null;
                sendError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    private void sendError(HttpStatus status, String errorMessage) {
        log.error("HTTP status: {} {}, Reason: {}", String.valueOf(status.getCode()), status.getMessage(), errorMessage);
        try {
            responseFacade.addConnection(false);
            if (status.getCode() >= 500) {
                responseFacade.sendError(status, status.getMessage());
            } else if (status.getCode() >= 400) {
                responseFacade.sendError(status, errorMessage);
            }
            responseFacade.flush();
        } catch (IOException e) {
            log.error("Failed to send error response: {}", e.getMessage());
        }
    }
}
