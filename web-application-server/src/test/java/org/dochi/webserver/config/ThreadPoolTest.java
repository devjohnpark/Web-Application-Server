package org.dochi.webserver.config;

import org.dochi.webserver.attribute.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolTest {

    private ThreadPool threadPool;

    @BeforeEach
    void setUp() {
        threadPool = new ThreadPool();
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
        assertEquals(200, threadPool.getMaxThreads());
    }
}