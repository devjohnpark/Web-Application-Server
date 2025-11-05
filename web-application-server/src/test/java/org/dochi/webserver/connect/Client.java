package org.dochi.webserver.connect;

import org.dochi.webserver.net.SocketWrapperBase;

import java.io.IOException;

public class Client {
    private final SocketWrapperBase<?> clientSocket;

    public Client(SocketWrapperBase<?> clientSocketWrapper) {
        this.clientSocket = clientSocketWrapper;
    }

    public void doRequest(byte[] input) throws IOException {
        clientSocket.write(input, 0, input.length);
        clientSocket.flush();
    }
}