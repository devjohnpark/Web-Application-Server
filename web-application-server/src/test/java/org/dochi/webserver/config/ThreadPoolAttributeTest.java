package org.dochi.webserver.config;

import org.dochi.webserver.attribute.ThreadPoolAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolAttributeTest {

    private ThreadPoolAttribute threadPool;

    @BeforeEach
    void setUp() {
        threadPool = new ThreadPoolAttribute();
    }

    @Test
    void setMinSpareThreads() {
        assertThrows(IllegalArgumentException.class, () -> threadPool.setMinSpareThreads(0));
        threadPool.setMinSpareThreads(100);
        assertEquals(100, threadPool.getMinSpareThreads());
    }

    @Test
    void setMaxThreads() {
        assertThrows(IllegalArgumentException.class, () -> threadPool.setMaxThreads(0));
        threadPool.setMaxThreads(100);
        assertEquals(100, threadPool.getMaxThreads());
    }

    @Test
    void getMaxThreads() {
        assertTrue(threadPool.getMaxThreads() > 0);
    }
}