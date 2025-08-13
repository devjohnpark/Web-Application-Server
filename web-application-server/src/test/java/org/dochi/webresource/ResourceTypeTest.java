//package org.dochi.webresource;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class ResourceTypeTest {
//
//    @Test
//    void getExtension() {
//        assertEquals(".txt", ResourceType.TEXT.getExtension());
//    }
//
//    @Test
//    void getMimeType() {
//        assertEquals("text/plain", ResourceType.TEXT.getMimeType());
//    }
//
//    @Test
//    void getContentType() {
//        assertEquals("text/plain; charset=UTF-8", ResourceType.TEXT.getContentType("UTF-8"));
//        assertEquals("multipart/form-data; boundary=2123123", ResourceType.MULTIPART.getContentType("2123123"));
//    }
//
//    @Test
//    void isEqual() {
//        assertTrue(ResourceType.TEXT.isEqual("text/plain; charset=UTF-8"));
//        assertTrue(ResourceType.TEXT.isEqual("text/plain;"));
//        assertTrue(ResourceType.TEXT.isEqual("text/plain"));
//    }
//
//    @Test
//    void getMediaType() {
//        assertEquals("UTF-8", ResourceType.TEXT.getMediaType("text/plain; charset=UTF-8"));
//        assertNull(ResourceType.TEXT.getMediaType("text/plain"));
//        assertNull(ResourceType.TEXT.getMediaType("text/plain;"));
//        assertNull(ResourceType.TEXT.getMediaType("text/plain; charset"));
//        assertNull(ResourceType.TEXT.getMediaType("text/plain; charset="));
//    }
//
//    @Test
//    void fromFilePath_support_file_format() {
//        String resourcePath = "/index.html";
//        assertThat(ResourceType.fromFilePath(resourcePath)).isEqualTo(ResourceType.HTML);
//    }
//
//    @Test
//    void fromFilePath_unsupported_file_format() {
//        String resourcePath = "/index.ht";
//        assertThat(ResourceType.fromFilePath(resourcePath)).isNull();
//    }
//}