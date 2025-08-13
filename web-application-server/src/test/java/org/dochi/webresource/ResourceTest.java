package org.dochi.webresource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTest {

//    @Test
//    void getData() {
//    }
//
//    @Test
//    void getContentType() {
//    }
//
//    @Test
//    void isEmpty() {
//    }

    @Test
    public void testDefaultConstructor() {
        Resource resource = new Resource();
        assertNull(resource.getData());
        assertTrue(resource.isEmpty());
    }

    @Test
    public void testParameterizedConstructor() {
        byte[] data = "Hello, World!".getBytes();
        String mimeType = "text/plain";

        Resource resource = new Resource(data, mimeType);
        assertArrayEquals(data, resource.getData());
        assertFalse(resource.isEmpty());
    }

    @Test
    public void testGetContentTypeWithCharset() {
        Resource resource = new Resource("Hello".getBytes(), "text/plain");
        String charset = "UTF-8";

        String contentType = resource.getContentType(charset);
        assertEquals("text/plain; charset=UTF-8", contentType);
    }

    @Test
    public void testGetContentTypeWithBoundary() {
        Resource resource = new Resource("--boundary--".getBytes(), "multipart/form-data");
        String boundary = "abc123";

        String contentType = resource.getContentType(boundary);
        assertEquals("multipart/form-data; boundary=abc123", contentType);
    }

    @Test
    public void testGetContentTypeWithoutParameter() {
        Resource resource = new Resource("<html></html>".getBytes(), "application/json");

        String contentType = resource.getContentType(null);
        assertEquals("application/json", contentType);
    }

    @Test
    public void testIsEmptyWhenDataIsNull() {
        Resource resource = new Resource(null, "text/plain");
        assertTrue(resource.isEmpty());
    }

    @Test
    public void testIsEmptyWhenMimeTypeIsNull() {
        Resource resource = new Resource("Hello".getBytes(), null);
        assertTrue(!resource.isEmpty());
    }

    @Test
    public void testIsEmptyWhenBothFieldsAreNull() {
        Resource resource = new Resource(null, null);
        assertTrue(resource.isEmpty());
    }

    @Test
    public void testIsEmptyWhenBothFieldsAreNotNull() {
        Resource resource = new Resource("Hello".getBytes(), "text/plain");
        assertFalse(resource.isEmpty());
    }
}