package org.dochi.webserver.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public final class Acceptor<S> implements Runnable, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(Acceptor.class);
//    private final ServerSocket listenSocket;
//    private final SocketTaskPoolExecutor executor;
//    private final ServerConfig config;
    private volatile boolean running;

//    public Acceptor(ServerSocket listenSocket, ServerConfig config) {
//        this.listenSocket = listenSocket;
//        this.executor = SocketTaskExecutorFactory.getInstance().createExecutor(config);
//        this.config = config;
//    }

    private final EndpointBase<S> endpoint;

    public Acceptor(EndpointBase<S> endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void run() {
        running = true;
        try {
            endpoint.bind();
            while (running) {
                S socket = endpoint.serverSocketAccept();
                if (!endpoint.processSocket(socket)) {
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
//        try {
//            if (executor != null) executor.shutdownGracefully();
//        } catch (Exception ex) {
//            log.warn("Executor shutdown failed", ex);
//        }
    }

//    @Override
//    public void run() {
//        running = true;
//        try {
//            while (running) {
//                final Socket socket = listenSocket.accept(); // close() 시 SocketException 발생
//                log.info("Accepted [Client IP: {}, Port: {}]", socket.getInetAddress(), socket.getPort());
//                executor.execute(new BioSocketWrapper(socket, config.getKeepAlive()));
//            }
//        } catch (IOException e) {
//            if (running) log.error("Accept failed", e);
//        }
//    }
//
//    @Override
//    public void close() throws IOException {
//        running = false;
//        listenSocket.close(); // accept 깨움
//        log.info("Closed listen socket");
//        try {
//            if (executor != null) executor.shutdownGracefully();
//        } catch (Exception ex) {
//            log.warn("Executor shutdown failed", ex);
//        }
//    }
}
