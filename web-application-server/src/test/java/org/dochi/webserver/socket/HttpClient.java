package org.dochi.webserver.socket;

import java.io.IOException;

public class HttpClient {
    private final SocketWrapper<?> clientSocket;

    public HttpClient(SocketWrapper<?> clientSocketWrapper) {
        this.clientSocket = clientSocketWrapper;
    }

    public void doRequest(byte[] input) throws IOException {
        clientSocket.write(input, 0, input.length);
        clientSocket.flush();
    }
}