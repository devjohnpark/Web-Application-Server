package org.dochi.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import java.net.Socket;

public class BioEndpoint extends AbstractEndpoint<Socket> {
    private static final Logger log = LoggerFactory.getLogger(BioEndpoint.class);

    private ServerSocket serverSocket;

    public BioEndpoint(int port, String hostName) {
        super(port, hostName);
    }

    @Override
    protected void bind() throws IOException {
        if (serverSocket != null) return;
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(hostName, port));
    }

    @Override
    public Socket serverSocketAccept() throws IOException {
        Socket socket = serverSocket.accept();
        log.debug("Accepted Client IP: {}, Port: {}", socket.getInetAddress().getHostAddress(), socket.getPort());
        return socket;
    }

    protected class BioSocketTask extends AbstractSocketTask<Socket> {

        public BioSocketTask(AbstractSocketWrapper<Socket> socketWrapper) {
            super(socketWrapper);
        }

        @Override
        protected void doRun() {
            if (handler.process(socketWrapper) == Handler.SocketState.CLOSED) {
                socketWrapper.close();
            }
            socketTaskPool.addFirst(this);
        }
    }

    @Override
    protected BioSocketTask createSocketTask(AbstractSocketWrapper<Socket> socketWrapper) {
        return new BioSocketTask(socketWrapper);
    }

    @Override
    protected AbstractSocketWrapper<Socket> wrapSocket(Socket socket) {
        return new BioSocketWrapper(socket, socketConfig);
    }

    @Override
    public void closeServerSocket() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}
