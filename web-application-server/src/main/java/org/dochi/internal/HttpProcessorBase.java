package org.dochi.internal;

//import org.dochi.connector.Http11ResponseFacade;
//import org.dochi.connector.Http11ResponseFacade;
import org.dochi.connector.Adapter;
import org.dochi.http.utils.HttpStatus;

import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.net.SocketWrapperBase;
//import org.dochi.webserver.socket.SocketState;
import org.dochi.webserver.net.EndpointBase.Handler.SocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

//import static org.dochi.webserver.socket.SocketState.CLOSED;

public abstract class HttpProcessorBase implements HttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(HttpProcessorBase.class);
    protected final Request request;
    protected final Response response;
    protected final Adapter adapter;

    protected HttpProcessorBase(Adapter adapter, HttpConfig config) {
        this.adapter = adapter;
        this.request = new Request();
        this.response = new Response();
//        this.requestFacade = new AbstractRequestFacade(config.getHttpReqConfig());
//        this.responseFacade = new Http11ResponseFacade(config.getHttpResConfig());
    }

    @Override
    public SocketState process(SocketWrapperBase<?> socketWrapper) throws IOException {
        setSocketWrapper(socketWrapper);
        try {
            return service(socketWrapper);
        } catch (Exception e) {
            resolveException(e);
        } finally {
            log.info("Process count: {}", socketWrapper.getKeepAliveCount());
        }
        return SocketState.CLOSED;
    }

    // 해당 클래스에서 생성하고 소유하는 객체의 컨텍스트 담당
    @Override
    public void recycle() {
        request.recycle();
        response.recycle();
    }

    protected Adapter getAdapter() {
        return adapter;
    }

    abstract protected void setSocketWrapper(SocketWrapperBase<?> socketWrapper);

    protected abstract SocketState service(SocketWrapperBase<?> socketWrapper) throws IOException;

    protected abstract boolean isKeepAlive(SocketWrapperBase<?> socketWrapper);

    // IllegalArgumentException는 클라이언트 잘못
    private void resolveException(Exception e) {
        switch (e) {
            case IllegalArgumentException illegalArgumentException -> { // wrong input from client
                // WAS internal input or parsing exception, I decide close connection
                sendClosedError(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            case SocketTimeoutException socketTimeoutException -> {
                // SocketTimeoutException exception thrown when valid time expires while being blocked by read() method of SocketInputStream object (write() is not related to setSoTimeout)
                // 408 must be closed
                sendClosedError(HttpStatus.REQUEST_TIMEOUT, e.getMessage());
            }
            case SocketException socketException -> {
                // reference: NioSocketImpl.implRead()
                //  If call Socket.read() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Connection reset") internally in Socket
                //  If call Socket.write() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Socket closed") internally in Socket
                log.error("Socket was read or write after the client closed connection: ", e);
            }
            case null, default -> { // IOException, RuntimeException, Exception
                assert e != null;
                // All other exceptions as internal server error then close
                sendClosedError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    private void sendClosedError(HttpStatus status, String errorMessage) {
        log.error("HTTP status: {} {}, Reason: {}", String.valueOf(status.getCode()), status.getMessage(), errorMessage);
        try {
            response.setStatus(status);
            response.setConnection("close");
            response.commit();
            response.flush();
        } catch (IOException e) {
            log.error("Failed to send error response: {}", e.getMessage());
        }
    }
}
