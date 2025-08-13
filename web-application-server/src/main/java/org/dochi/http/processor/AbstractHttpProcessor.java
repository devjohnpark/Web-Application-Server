package org.dochi.http.processor;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.request.processor.HttpRequestProcessor;
import org.dochi.http.response.processor.HttpResponseProcessor;
import org.dochi.http.response.HttpStatus;
import org.dochi.webserver.socket.SocketState;
import org.dochi.webserver.socket.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static org.dochi.webserver.socket.SocketState.*;

public abstract class AbstractHttpProcessor implements HttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpProcessor.class);
    protected final HttpRequestProcessor request;
    protected final HttpResponseProcessor response;

    protected AbstractHttpProcessor(HttpRequestProcessor request, HttpResponseProcessor response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public SocketState process(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper) {
        SocketState state = CLOSED;
        try {
            // Recycling object's sharing resource cannot match the main memory with cpu cache in multithreading environment.
            // I choose recycling object initialization cuz volatile variable for memory visibility has overhead.
            recycle(); // memory visibility
            state = service(socketWrapper, httpApiMapper);
        } catch (Exception e) {
            processException(e);
            safeRecycle();
        }
        return state;
    }

    protected abstract SocketState service(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper) throws IOException;

    protected abstract boolean shouldPersistentConnection(SocketWrapper socketWrapper);

    protected void recycle() throws IOException {
        request.recycle();
        response.recycle();
    }

    protected void safeRecycle() {
        try {
            recycle();
        } catch (IOException e) {
            log.error("Recycle failed: ", e);
        }
    }

    // Because the developer has the option to handle RuntimeException, RuntimeException propagated by not catching it is considered to be an invalid request from the client and a 400 response is sent.
    // Unexpected IOException on input/output, 500 response because Exception is a server problem.
    protected void processException(Exception e) {
        switch (e) {
            case SocketTimeoutException socketTimeoutException -> {
//                SocketTimeoutException exception thrown when valid time expires while being blocked by read() method of SocketInputStream object (write() is not related to setSoTimeout)
                log.error("Socket read timeout occurred: ", e);
//              sendError(HttpStatus.REQUEST_TIMEOUT, e.getMessage()); // non necessary need response when socket read timeout
            }
            case SocketException socketException -> {
                // reference: NioSocketImpl.implRead()
                //  If call Socket.read() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Connection reset") internally in Socket
                //  If call Socket.write() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Socket closed") internally in Socket
                log.error("Socket was read or write after the client closed connection: ", e);
            }
            case HttpStatusException httpStatusException -> {
                sendError(httpStatusException.getHttpStatus(), e.getMessage());
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
            if (status.getCode() >= 500) {
                response.sendError(status, status.getMessage());
            } else if (status.getCode() >= 400) {
                response.sendError(status, errorMessage);
            }
            response.flush();
        } catch (IOException e) {
            log.error("Failed to send error response: {}", e.getMessage());
        }
    }
}
