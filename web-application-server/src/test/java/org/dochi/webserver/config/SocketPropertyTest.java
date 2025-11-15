package org.dochi.webserver.config;

import org.dochi.webserver.property.SocketProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SocketPropertyTest {

    private SocketProperty socketProperty;

    @BeforeEach
    void setUp() {
        socketProperty = new SocketProperty();
    }

    @Test
    void set_get_KeepAliveTimeout() {
        socketProperty.setKeepAliveTimeout(3000);
        assertEquals(3000, socketProperty.getKeepAliveTimeout());
    }

    @Test
    void get_set_MaxKeepAliveRequests() {
        socketProperty.setMaxKeepAliveRequests(600);
        assertEquals(600, socketProperty.getMaxKeepAliveRequests());
    }
}