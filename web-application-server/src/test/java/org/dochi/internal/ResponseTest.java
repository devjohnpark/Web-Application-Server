package org.dochi.internal;

import org.dochi.http.utils.HttpStatus;
import org.dochi.http.utils.HttpVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    private final Response response = new Response();
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        response.init(out);
    }

    @AfterEach
    void tearDown() {
        response.recycle();
    }

    private static String asIso88591(ByteArrayOutputStream out) {
        return out.toString(StandardCharsets.ISO_8859_1);
    }

    @Test
    void commitWritesStatusLineHeadersFinalCRLFAndSetsCommittedTrue() throws Exception {
        response.setVersion(HttpVersion.HTTP_1_1);
        response.setStatus(HttpStatus.OK);
        response.setContentType("text/plain");
        response.setContentLength(0);

        response.commit();

        assertTrue(response.isCommitted());

        String msg = asIso88591(out);

        assertTrue(msg.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(msg.contains("Content-Type: text/plain\r\n"));
        assertTrue(msg.contains("Content-Length: 0\r\n"));
        assertTrue(msg.contains("Date: "));
        assertTrue(msg.endsWith("\r\n\r\n"));
    }

    @Test
    void commitMessageWritesBodyAfterHeaderTerminator() throws Exception {
        response.setStatus(HttpStatus.OK);
        response.setContentType("text/plain");
        byte[] body = "hello".getBytes(StandardCharsets.ISO_8859_1);

        response.commitMessage(body);

        String msg = asIso88591(out);

        int headerEnd = msg.indexOf("\r\n\r\n");
        assertTrue(headerEnd >= 0);

        String bodyPart = msg.substring(headerEnd + 4);
        assertEquals("hello", bodyPart);
    }

    @Test
    void commitIsIdempotentAndSecondCommitWritesNothingMore() throws Exception {
        response.setStatus(HttpStatus.OK);
        response.commit();
        int firstLen = out.size();

        response.commit();
        int secondLen = out.size();

        assertEquals(firstLen, secondLen);
    }

    @Test
    void settingStatusOrHeadersAfterCommitThrowsException() throws Exception {
        response.commit();
        assertThrows(IllegalStateException.class, () -> response.setStatus(HttpStatus.NOT_FOUND));
        assertThrows(IllegalStateException.class, () -> response.setContentType("application/json"));
        assertThrows(IllegalStateException.class, () -> response.setContentLength(10));
    }

    @Test
    void recycleResetsState() throws Exception {
        Response res = new Response();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.init(out);

        res.setVersion(HttpVersion.HTTP_1_0);
        res.setStatus(HttpStatus.NOT_FOUND);
        res.commit();

        res.recycle();

        assertFalse(res.isCommitted());
        assertEquals(HttpVersion.HTTP_1_1, res.getVersion());
        assertEquals(HttpStatus.OK, res.getStatus());
        assertTrue(res.getHeaders().getHeaders().isEmpty());
    }
}
