package org.dochi.webserver.protocol;

import org.dochi.connector.HttpDispatcher;
import org.dochi.internal.processor.HttpProcessor;
import org.dochi.webserver.config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpProtocolHandlerTest {
    HttpProtocolHandler protocolHandler;
    ServerConfig serverConfig = new ServerConfig();
    HttpConfig httpConfig = new HttpConfigImpl(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute());
    HttpDispatcher httpDispatcher = new HttpDispatcher(serverConfig.getWebService());

    @BeforeEach
    void init() {
        protocolHandler = new HttpProtocolHandler(httpDispatcher, httpConfig);
    }

    @Test
    void getProcessor() {
        HttpProcessor processor = protocolHandler.getProcessor();
        assertEquals(0, protocolHandler.getSize("HTTP/1.1"));
    }

    @Test
    void release() {
        HttpProcessor processor = protocolHandler.getProcessor(); // 0
        protocolHandler.release(processor); // 1
        processor = protocolHandler.getProcessor(); // 0
        protocolHandler.release(processor); // 1
        assertEquals(1, protocolHandler.getSize("HTTP/1.1"));
    }

    @Test
    void getSize() {
        assertEquals(protocolHandler.getSize("HTTP/1.1"), 0);
    }
}