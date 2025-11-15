package org.dochi.connector;

import org.dochi.http.utils.HttpStatus;
import org.dochi.webresource.ResourceType;
import org.dochi.webserver.property.HttpResponseProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {
    private final org.dochi.internal.Response internalResponse = new org.dochi.internal.Response();
    private final Response externalResponse = new Response(internalResponse, new HttpResponseProperty());
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        internalResponse.setOutputStream(out);

        // low-level(interna.Response) facade 연결 (set OutputStrea, can commit )
        internalResponse.setFacade(externalResponse);
    }

    @AfterEach
    void tearDown() {
        internalResponse.recycle();
        externalResponse.recycle();
    }

    @Test
    void sendByteArraySetsContentLengthAndContentTypeWhenMissing() throws Exception {
        byte[] body = "hello".getBytes(StandardCharsets.UTF_8);

        externalResponse.send(body, "text/plain");

        String msg = out.toString(StandardCharsets.ISO_8859_1);

        assertTrue(msg.contains("Content-Length: " + body.length + "\r\n"));
        assertTrue(msg.contains("Content-Type: text/plain\r\n"));

        // body가 header 뒤에 붙는지 확인
        assertBodyEquals(body);
    }

    @Test
    void sendDoesNotOverrideExistingContentLengthOrContentType() throws Exception {
        byte[] body = "hello".getBytes(StandardCharsets.UTF_8);

        // 미리 세팅해두면 send()가 덮어쓰면 안 됨
        externalResponse.setContentLength(999);
        externalResponse.setContentType("application/json");

        externalResponse.send(body, "text/plain");

        String msg = out.toString(StandardCharsets.ISO_8859_1);

        assertTrue(msg.contains("Content-Length: 999\r\n"));
        assertTrue(msg.contains("Content-Type: application/json\r\n"));
        assertFalse(msg.contains("Content-Type: text/plain\r\n"));

        assertBodyEquals(body);
    }

    @Test
    void sendStringUsesUtf8ByDefault() throws Exception {
        String body = "가나다";
        byte[] expected = body.getBytes(StandardCharsets.UTF_8);

        externalResponse.send(body, "text/plain; charset=utf-8");

        assertBodyEquals(expected);
    }

    @Test
    void sendCharArrayUsesUtf8ByDefault() throws Exception {
        char[] body = "hello".toCharArray();
        byte[] expected = "hello".getBytes(StandardCharsets.UTF_8);

        externalResponse.send(body, "text/plain");

        assertBodyEquals(expected);
    }

    @Test
    void sendDoesNothingForBodyNotAllowedStatus_204() throws Exception {
        byte[] body = "hello".getBytes(StandardCharsets.UTF_8);

        externalResponse.setStatus(HttpStatus.NO_CONTENT); // 204
        externalResponse.send(body, "text/plain");

        String msg = out.toString(StandardCharsets.ISO_8859_1);
        assertFalse(msg.contains("Content-Length: "));
        assertFalse(msg.contains("Content-Type: "));

        assertBodyEquals(body);
    }

    @Test
    void sendErrorWithNonErrorStatusThrows() {
        assertThrows(IllegalArgumentException.class, () -> externalResponse.sendError(HttpStatus.OK));
    }

    @Test
    void sendErrorWritesStatusAndDefaultTextContentType() throws Exception {
        externalResponse.sendError(HttpStatus.NOT_FOUND, "not found");

        String msg = out.toString(StandardCharsets.ISO_8859_1);

        // status line 체크
        assertTrue(msg.startsWith("HTTP/1.1 404 Not Found\r\n"));

        // content-type은 ResourceType.TEXT.getContentType("utf-8")로 들어가야 함
        String expectedContentType = ResourceType.TEXT.getContentType("utf-8");
        assertTrue(msg.contains("Content-Type: " + expectedContentType + "\r\n"));

        // body 체크 (UTF-8)
        assertBodyEquals("not found".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void getOutputStreamCommitsHeaderAndReturnsSameOutputStream() throws Exception {
        // getOutputStream()은 commit()을 호출하고 out을 반환해야 함
        assertSame(out, externalResponse.getOutputStream());

        String msg = out.toString(StandardCharsets.ISO_8859_1);
        assertTrue(msg.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(msg.contains("\r\n\r\n"));
    }

    private void assertBodyEquals(byte[] expectedBody) {
        byte[] all = out.toByteArray();
        byte[] sep = "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1);
        int headerEnd = indexOf(all, sep);
        assertTrue(headerEnd >= 0);

        int bodyStart = headerEnd + sep.length;
        byte[] actualBody = Arrays.copyOfRange(all, bodyStart, all.length);

        assertArrayEquals(expectedBody, actualBody);
    }

    private static int indexOf(byte[] src, byte[] target) {
        for (int i = 0; i <= src.length - target.length; i++) {
            boolean matched = true;

            for (int j = 0; j < target.length; j++) {
                if (src[i + j] != target[j]) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                return i;
            }
        }
        return -1;
    }

}
