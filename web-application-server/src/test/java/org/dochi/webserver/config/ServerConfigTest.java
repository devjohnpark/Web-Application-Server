package org.dochi.webserver.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    private ServerConfig serverConfig;

    @BeforeEach
    void setUp() {
        serverConfig = new ServerConfig();
    }
    @Test
    void getWebService() {
        assertNotNull(serverConfig.getWebService());
    }

    @Test
    void getKeepAlive() {
        assertNotNull(serverConfig.getKeepAlive());
    }
}