package org.dochi.webserver.socket;

import org.dochi.webserver.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public final class Connector implements Runnable, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);
    private final ServerSocket listenSocket;
    private final SocketTaskExecutor executor;
    private final ServerConfig config;
    private volatile boolean running;

    public Connector(ServerSocket listenSocket, ServerConfig config) {
        this.listenSocket = listenSocket;
        this.executor = SocketTaskExecutorFactory.getInstance().createExecutor(config);
        this.config = config;
    }

    @Override
    public void run() {
        running = true;
        try {
            while (running) {
                final Socket socket = listenSocket.accept(); // close() 시 SocketException 발생
                log.info("Accepted [Client IP: {}, Port: {}]", socket.getInetAddress(), socket.getPort());
                executor.execute(new BioSocketWrapper(socket, config.getKeepAlive()));
            }
        } catch (IOException e) {
            if (running) log.error("Accept failed", e);
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        listenSocket.close(); // accept 깨움
        log.info("Closed listen socket");
        try {
            if (executor != null) executor.shutdownGracefully();
        } catch (Exception ex) {
            log.warn("Executor shutdown failed", ex);
        }
    }
}
