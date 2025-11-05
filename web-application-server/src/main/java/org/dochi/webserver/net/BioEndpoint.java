package org.dochi.webserver.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import java.net.Socket;

public class BioEndpoint extends EndpointBase<Socket> {
    private static final Logger log = LoggerFactory.getLogger(BioEndpoint.class);

    private ServerSocket serverSocket;

    public BioEndpoint(int port, String hostName) {
        super(port, hostName);
    }

    @Override
    public void bind() throws IOException {
        if (serverSocket != null) return;
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(hostName, port));
    }

    @Override
    public Socket serverSocketAccept() throws IOException {
        Socket socket = serverSocket.accept();
        log.info("Accepted Client IP: {}, Port: {}", socket.getInetAddress().getAddress(), socket.getPort());
        return socket;
    }

    protected class BioSocketTask extends SocketTaskBase<Socket> {

        public BioSocketTask(SocketWrapperBase<Socket> socketWrapper) {
            super(socketWrapper);
        }

        @Override
        protected void doRun() {
            if (handler.process(socketWrapper) == Handler.SocketState.CLOSED) {
                socketWrapper.close();
            }
        }
    }

    @Override
    protected BioSocketTask createSocketTask(SocketWrapperBase<Socket> socketWrapper) {
        return new BioSocketTask(socketWrapper);
    }

    @Override
    protected SocketWrapperBase<Socket> wrapSocket(Socket socket) {
        return new BioSocketWrapper(socket, getSocketConfig());
    }

    @Override
    public void closeServerSocket() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close(); // accept 깨움
        }
    }
}
