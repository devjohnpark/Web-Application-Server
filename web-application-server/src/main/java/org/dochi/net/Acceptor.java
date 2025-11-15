package org.dochi.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class Acceptor<S> implements Runnable, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(Acceptor.class);
    private volatile boolean running;
    private final AbstractEndpoint<S> endpoint;

    public Acceptor(AbstractEndpoint<S> endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void run() {
        running = true;
        try {
            while (running) {
                S socket = endpoint.serverSocketAccept();
                if (!endpoint.processSocketTask(socket)) {
                    endpoint.closeSocket(socket);
                }
            }
        } catch (IOException e) {
            if (running) log.error("Accept failed", e);
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        endpoint.closeServerSocket();
    }
}
