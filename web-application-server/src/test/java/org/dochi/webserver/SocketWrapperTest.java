//package org.dochi.webserver;
//
//import org.dochi.webserver.socket.BioSocketWrapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//
//import java.net.Socket;
//
//class SocketWrapperTest {
//    private BioSocketWrapper socketWrapper;
//
//    @BeforeEach
//    void setUp() {
//        socketWrapper = new BioSocketWrapper(1000, 100);
//    }
//
//    @Test
//    void setSocket() {
//        socketWrapper.setSocket(new Socket());
//        assertNotNull(socketWrapper.getSocket());
//    }
//
//    @Test
//    void getSocket() {
//        assertThrows(IllegalStateException.class, () -> socketWrapper.getSocket());
//    }
//
//    @Test
//    void getKeepAlive() {
//        assertEquals(1000, socketWrapper.getKeepAliveTimeout());
//        assertEquals(100, socketWrapper.getMaxKeepAliveRequests());
//    }
//
//    @Test
//    void isUsing() {
//        assertFalse(socketWrapper.isUsing());
//        socketWrapper.setSocket(new Socket());
//        assertTrue(socketWrapper.isUsing());
//    }
//}