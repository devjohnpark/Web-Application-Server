package org.dochi.webserver.config;

import org.dochi.webserver.attribute.KeepAlive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeepAliveTest {

    private KeepAlive keepAlive;

    @BeforeEach
    void setUp() {
        keepAlive = new KeepAlive();
    }

    @Test
    void set_get_KeepAliveTimeout() {
        keepAlive.setKeepAliveTimeout(3000);
        assertEquals(3000, keepAlive.getKeepAliveTimeout());
    }

    @Test
    void get_set_MaxKeepAliveRequests() {
        keepAlive.setMaxKeepAliveRequests(600);
        assertEquals(600, keepAlive.getMaxKeepAliveRequests());
    }
}