//package org.dochi.http.response;
//
//import org.dochi.http.api.Request;
//import org.dochi.http.request.data.HttpVersion;
//import org.dochi.http.request.data.MimeHeaders;
//import org.dochi.webresource.ResourceType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//class HttpResponseTest {
//    // add headers -> send -> get data of OutputStream -> check
//    // OutputStream.toString(): ByteArrayOutputStream 클래스의 메서드로, 스트림에 쓰여진 바이트 데이터를 문자열로 변환하여 반환
//    private ByteArrayOutputStream outputStream;
//    private Response response;
//    private Path tempDir;
//
//    @BeforeEach
//    void setUp() {
//        outputStream = new ByteArrayOutputStream();
//        response = new Response(outputStream);
//        response.inActiveDateHeader();
//    }
//
//    @Test
//    void send() throws IOException {
////        response.send(HttpStatus.OK);
//
//        String result = outputStream.toString();
//        assertTrue(result.startsWith("HTTP/1.1 200 OK\r\n"));
//        assertTrue(result.endsWith("\r\n\r\n"));
//    }
//
//
//    @Test
//    void send_body() throws IOException {
//        String body = "Hello, World!";
//        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
//
//        response.send(HttpStatus.OK, bodyBytes, ResourceType.TEXT.getContentType("UTF-8"));
//
//        String result = outputStream.toString();
//        assertTrue(result.contains("Content-Type: " + ResourceType.TEXT.getContentType("UTF-8")));
//        assertTrue(result.contains("Content-Length: " + bodyBytes.length));
//        assertTrue(result.contains(body));
//    }
//
//    @Test
//    void sendError() throws IOException {
//        HttpStatus status = HttpStatus.NOT_FOUND;
//        response.sendError(HttpStatus.NOT_FOUND);
//
//        String result = outputStream.toString();
//        assertTrue(result.startsWith("HTTP/1.1 404 Not Found\r\n"));
//        assertTrue(result.contains("Content-Type: " + ResourceType.TEXT.getMimeType()));
//        assertTrue(result.contains("Content-Length: " + status.getDescription().length()));
//        assertTrue(result.contains(HttpStatus.NOT_FOUND.getDescription()));
//    }
//
//    @Test
//    void sendError_errorMessage() throws IOException {
//        String errorMessage = "error message";
//
//        response.sendError(HttpStatus.BAD_REQUEST, errorMessage);
//
//        String result = outputStream.toString();
//        assertTrue(result.contains("Content-Type: " + ResourceType.TEXT.getMimeType()));
//        assertTrue(result.contains("Content-Length: " + errorMessage.length()));
//        assertTrue(result.contains(errorMessage));
//    }
//
//    @Test
//    void send_body_null() throws IOException {
//        response.send(HttpStatus.OK, null, null);
//
//        String result = outputStream.toString();
//        assertFalse(result.contains("Content-Type:"));
//        assertFalse(result.contains("Content-Length:"));
//    }
//
//    @Test
//    void addHeader() throws IOException {
//        response.addHeader("Last-Modified", "Sat, 07 Feb xxxx");
//
////        response.send(HttpStatus.OK);
//
//        String result = outputStream.toString();
//        assertTrue(result.contains("Last-Modified: " + "Sat, 07 Feb xxxx"));
//    }
//
//    @Test
//    void addVersion() throws IOException {
//        response.addVersion(HttpVersion.HTTP_1_1);
//
////        response.send(HttpStatus.OK);
//
//        String result = outputStream.toString();
//        assertTrue(result.contains("HTTP/1.1"));
//    }
//}