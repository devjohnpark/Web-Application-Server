package org.dochi.webserver.connect;

import org.dochi.webserver.socket.SocketWrapper;

import java.io.IOException;

public class Client {
    private final SocketWrapper<?> clientSocket;

    public Client(SocketWrapper<?> clientSocketWrapper) {
        this.clientSocket = clientSocketWrapper;
    }

    public void doRequest(byte[] input) throws IOException {
        clientSocket.write(input, 0, input.length);
        clientSocket.flush();
    }
}