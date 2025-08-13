package org.dochi.webserver;

import org.dochi.webserver.attribute.WebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebServerTest {
    private WebServer webServer;

    @BeforeEach
    void setUp() {
        webServer = new WebServer();
    }

    @Test
    void getHostName() {
        assertEquals("localhost", webServer.getHostName());
    }

    @Test
    void getPort() {
        assertEquals(8080, webServer.getPort());
    }

    @Test
    void getConfig() {
        assertNotNull(webServer.getConfig());
    }
}