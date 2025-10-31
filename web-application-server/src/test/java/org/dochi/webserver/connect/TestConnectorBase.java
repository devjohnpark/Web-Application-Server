package org.dochi.webserver.connect;

import org.dochi.internal.http11.Http11InputBuffer;
import org.dochi.internal.request.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public abstract class TestConnectorBase {
    private final Connector connector = new Connector();
    protected Client client;
    protected final Request request = new Request();
    protected final int headerMaxSize = 1024;
    protected final Http11InputBuffer inputBuffer = new Http11InputBuffer(headerMaxSize);

    @BeforeEach
    void setUp() throws IOException {
        connector.connect();
        inputBuffer.init(connector.getServerConnectedSocket());
        client = new Client(connector.getClientConnectedSocket());
        setUpInternal();
    }

    @AfterEach
    void tearDown() throws IOException {
        inputBuffer.recycle();
        request.recycle();
        connector.disconnect();
        tearDownInternal();
    }

    protected abstract void setUpInternal() throws IOException;
    protected abstract void tearDownInternal() throws IOException;
}
