//package org.dochi.webserver.executor;
//
//import org.dochi.webserver.attribute.WebServer;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class ServerExecutorTest {
//
//    @Test
//    void execute() {
//        assertThrows(IllegalStateException.class, ServerExecutor::execute);
//    }
//
//    @Test
//    void addWebServer() {
//        ServerExecutor.addWebServer(new WebServer(8080));
//        assertThrows(IllegalArgumentException.class, () -> ServerExecutor.addWebServer(new WebServer(8080)));
//    }
//}