package org.dochi.webserver.connect;

import org.dochi.net.AbstractSocketWrapper;

import java.io.IOException;

public class Client {
    private final AbstractSocketWrapper<?> clientSocket;

    public Client(AbstractSocketWrapper<?> clientSocketWrapper) {
        this.clientSocket = clientSocketWrapper;
    }

    public void doRequest(byte[] input) throws IOException {
        clientSocket.write(input, 0, input.length);
        clientSocket.flush();
    }
}