package org.dochi.webserver.connect;

import org.dochi.internal.buffer.TmpBufferedOutputStream;
import org.dochi.internal.http11.Http11InputBuffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public abstract class TestConnectionBase {
    private final Connection connection = new Connection();
    protected Client client;
    protected final org.dochi.internal.request.Request request = new org.dochi.internal.request.Request();
    protected final int headerMaxSize = 1024;
    protected Http11InputBuffer inputBuffer = new Http11InputBuffer(headerMaxSize);

    @BeforeEach
    void setUp() throws IOException {
        connection.connect();
        inputBuffer.init(connection.getServerConnectedSocket());
        client = new Client(connection.getClientConnectedSocket());
        setUpInternal();
    }

    @AfterEach
    void tearDown() throws IOException {
        inputBuffer.recycle();
        request.recycle();
        connection.disconnect();
        tearDownInternal();
    }

    protected abstract void setUpInternal() throws IOException;
    protected abstract void tearDownInternal() throws IOException;
}
