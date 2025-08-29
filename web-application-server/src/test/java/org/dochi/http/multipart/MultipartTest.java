package org.dochi.http.multipart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MultipartTest {

    private Multipart multipart;

    @BeforeEach
    void setUp() {
        multipart = new Multipart();
    }

    @Test
    void addEmptyPart() {
        Part part = new Part();
        multipart.addPart("testPart", part);

        assertEquals(part, multipart.getPart("testPart"));
        assertNotNull(multipart.getPart("testPart"));
    }

    @Test
    void getPartWhenEmpty() throws IOException {
        Part part = multipart.getPart("nonExistent");
        assertNotNull(part);
        assertTrue(part.isEmpty());
    }

    @Test
    void getPartNonExistent() {
        Part part = new Part();
        multipart.addPart("part", part);

        Part retrieved = multipart.getPart("nonExistent");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void addGetPart() throws IOException {
        Part part = new Part("hello".getBytes(StandardCharsets.ISO_8859_1), "text/html");
        multipart.addPart("part", part);
        assertArrayEquals("hello".getBytes(StandardCharsets.ISO_8859_1), part.getContent());
        assertEquals("text/html", part.getContentType());
    }

    @Test
    void recycle() throws IOException {
        Part part = new Part("hello".getBytes(StandardCharsets.ISO_8859_1), "text/html");
        multipart.addPart("part", part);
        assertFalse(part.isEmpty());

        multipart.recycle();

        Part newPart = multipart.getPart("part");
        assertNotNull(newPart);
        assertTrue(newPart.isEmpty());
    }
}