package org.dochi.internal;

import org.dochi.connector.Adapter;
import org.dochi.http.utils.HttpStatus;
import org.dochi.net.AbstractSocketWrapper;
import org.dochi.net.AbstractEndpoint.Handler.SocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;


public abstract class AbstractHttpProcessor implements HttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpProcessor.class);
    protected final Request request;
    protected final Response response;
    protected final Adapter adapter;

    public AbstractHttpProcessor(Adapter adapter) {
        this(adapter, new Request(), new Response());
    }

    protected AbstractHttpProcessor(Adapter adapter, Request request, Response response) {
        this.adapter = adapter;
        this.request = request;
        this.response = response;
    }

    @Override
    public SocketState process(AbstractSocketWrapper<?> socketWrapper) throws IOException {
        setSocketWrapper(socketWrapper);
        try {
            return service(socketWrapper);
        } catch (IllegalArgumentException e) { // WebAppServer internal input or parsing exception, I decide close connection
            resolveClosedError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (SocketTimeoutException e) {
            // SocketTimeoutException: when valid time expires while being blocked by read() method of SocketInputStream object (write() is not related to setSoTimeout)
            resolveClosedError(HttpStatus.REQUEST_TIMEOUT, e.getMessage());
        } catch (SocketException e) {
            // ignore (CLOSED)
            // reference: NioSocketImpl.implRead()
            //  If call Socket.read() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Connection reset") internally in Socket
            //  If call Socket.write() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Socket closed") internally in Socket
        } catch (Throwable t) { // remain all other exceptions as internal server error then close
            resolveClosedError(HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage());
        } finally {
            log.info("Process count: {}", socketWrapper.getKeepAliveCount());
        }
        return SocketState.CLOSED;
    }

    @Override
    public void recycle() {
        request.recycle();
        response.recycle();
    }

    protected Adapter getAdapter() {
        return adapter;
    }

    abstract protected void setSocketWrapper(AbstractSocketWrapper<?> socketWrapper);

    protected abstract SocketState service(AbstractSocketWrapper<?> socketWrapper) throws IOException;

    protected abstract boolean isKeepAlive(AbstractSocketWrapper<?> socketWrapper);

    private void resolveClosedError(HttpStatus status, String errorMessage) throws IOException {
        response.setStatus(status);
        response.setConnection("close");
        response.commit();
        response.flush();
    }
}
