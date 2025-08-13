package org.dochi.http.request;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.monitor.HttpMessageSizeManager;
import org.dochi.http.request.stream.Http11RequestStream;
import org.dochi.webserver.attribute.HttpReqAttribute;
import org.dochi.webserver.config.HttpReqConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Http11RequestStreamTest {
    HttpMessageSizeManager messageSizeManager;
    HttpReqConfig httpReqConfig;

    @BeforeEach
    void setUp() {
        httpReqConfig = new HttpReqConfig(new HttpReqAttribute());
        messageSizeManager = new HttpMessageSizeManager(httpReqConfig.getRequestHeaderMaxSize(), httpReqConfig.getRequestHeaderMaxSize());
    }

    @Test
    void readHeader() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
        Http11RequestStream http11RequestStream = new Http11RequestStream(byteArrayInputStream);
        assertEquals("HTTP/1.1 200 OK", http11RequestStream.readHeader(messageSizeManager.getHeaderMonitor()));
    }

//    @Test
//    void readLine_invalid_lineString() throws IOException {
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("HTTP/1.1 200 OK\n".getBytes(StandardCharsets.UTF_8));
//        Http11RequestStream http11RequestStream = new Http11RequestStream(byteArrayInputStream);
//        assertThrows(HttpStatusException.class, () -> http11RequestStream.readHeader(messageSizeManager.getHeaderMonitor()));
//    }

//    @Test
//    void readLine_String_EOF() throws IOException {
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
//        Http11RequestStream http11RequestStream = new Http11RequestStream(byteArrayInputStream);
//        assertNull(http11RequestStream.readHeader(messageSizeManager.getHeaderMonitor()));
//    }

    @Test
    void readLineBytes() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
        Http11RequestStream http11RequestStream = new Http11RequestStream(byteArrayInputStream);
        assertArrayEquals("HTTP/1.1 200 OK".getBytes(StandardCharsets.UTF_8),http11RequestStream.readLineBytes(messageSizeManager.getBodyMonitor()));
    }

//    @Test
//    void readLine_invalid_lineBytes() throws IOException {
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("HTTP/1.1 200 OK\n".getBytes(StandardCharsets.UTF_8));
//        Http11RequestStream http11RequestStream = new Http11RequestStream(byteArrayInputStream);
//        assertThrows(HttpStatusException.class, () -> http11RequestStream.readLineBytes(messageSizeManager.getBodyMonitor()));
//    }

//    @Test
//    void readLine_Bytes_EOF() throws IOException {
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
//        Http11RequestStream http11RequestStream = new Http11RequestStream(byteArrayInputStream);
//        assertNull(http11RequestStream.readLineBytes(messageSizeManager.getBodyMonitor()));
//    }

    @Test
    void readBody() throws IOException {
        byte[] body = "Hello world fdfdsfadfadsfadsfdfdasfdfdfaf fadsfadsfsfdsfadsfdfasf".getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        Http11RequestStream http11RequestStream = new Http11RequestStream(byteArrayInputStream);
        assertArrayEquals(body, http11RequestStream.readAllBody(body.length, messageSizeManager.getBodyMonitor()));
    }

//    @Test
//    void readLine() throws IOException {
//
//        String line;
//        while (!(line = http11RequestStream.readLine()).isEmpty()) {
//            System.out.println(line);
//        }
//
////        String line;
//        while (!(line = http11RequestStream.readLine()).isEmpty()) {
//            System.out.println(line);
//        }
////        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
////
//////        if (buffer.size() == 0 && b == -1) {
//////            return null;
//////        }
////        buffer.write('\r');
////        buffer.reset();
////        assertEquals("", buffer.toString(StandardCharsets.UTF_8));
//    }
//
//    @Test
//    void readLine2() throws IOException {
//        String testDir = "./src/test/resources/";
//        BufferedSocketInputStream in = new FileInputStream(new File(testDir + "http_req_get.txt"));
//        BufferedReader br = new BufferedReader(new InputStreamReader(in));
////        String str1 = br.readLine();
////        String str2 = br.readLine();
////        System.out.println(str1);
////        System.out.println(str2);
////        assertEquals("", str2);
//        String line;
//        while (!(line = br.readLine()).isEmpty()) {
//            System.out.println(line);
//        }
//
//        while (!(line = br.readLine()).isEmpty()) {
//            System.out.println(line);
//        }
//    }
}