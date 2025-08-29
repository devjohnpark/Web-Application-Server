package org.dochi.http.multipart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MultipartHeadersTest {
    
    private MultipartHeaders headers;
    
    @BeforeEach
    void setUp() {
        headers = new MultipartHeaders();
    }
    
    @Test
    void addHeaderWhenValidLineProvided() {
        headers.addHeader("Content-Type: text/plain");
        assertEquals("text/plain", headers.getHeader("content-type"));
    }

    @Test
    void ignoreHeaderWhenNullLineProvided() {
        headers.addHeader(null);
        assertNull(headers.getHeader("content-type"));
    }

    @Test
    void overwriteExistingHeaderWithSameKey() {
        headers.addHeader("Content-Type: text/plain");
        headers.addHeader("Content-Type: application/json");
        assertEquals("application/json", headers.getHeader("content-type"));
    }
    
    @Test
    void addHeaderWhenValidNameAndValueProvided() {
        headers.addHeader("Content-Type", "text/html");
        assertEquals("text/html", headers.getHeader("content-type"));
    }

    @Test
    void ignoreHeaderWhenNullValueProvided() {
        headers.addHeader("Content-Type", null);
        assertNull(headers.getHeader("content-type"));
    }

    @Test
    void ignoreHeaderWhenEmptyValueProvided() {
        headers.addHeader("Content-Type", "");
        assertNull(headers.getHeader("content-type"));
    }

    @Test
    void storeNameInLowerCaseForNameValueMethod() {
        headers.addHeader("CONTENT-DISPOSITION", "form-data; name=\"file\"");
        assertEquals("form-data; name=\"file\"", headers.getHeader("content-disposition"));
    }

    @Test
    void shouldReturnNullWhenHeaderNotFound() {
        assertNull(headers.getHeader("non-existent"));
    }

    @Test
    void shouldReturnHeaderValueWhenFound() {
        headers.addHeader("Custom-Header", "custom-value");
        assertEquals("custom-value", headers.getHeader("custom-header"));
    }

    @Test
    void shouldReturnNullWhenSearchingWithUpperCaseKey() {
        headers.addHeader("Content-Type", "text/plain");
        assertEquals("text/plain", headers.getHeader("Content-Type"));
        assertEquals("text/plain", headers.getHeader("content-type"));
    }
    
    @Test
    void shouldReturnContentDispositionValue() {
        headers.addHeader("Content-Disposition", "form-data; name=\"username\"");
        assertEquals("form-data; name=\"username\"", headers.getContentDisposition());
    }

    @Test
    void shouldClearAllHeadersWhenRecycled() {
        headers.addHeader("Content-Type", "text/plain");
        headers.addHeader("Content-Disposition", "form-data");
        
        headers.recycle();
        
        assertNull(headers.getHeader("content-type"));
        assertNull(headers.getHeader("content-disposition"));
        assertNull(headers.getContentType());
        assertNull(headers.getContentDisposition());
    }

    @Test
    void shouldAllowAddingHeadersAfterRecycle() {
        headers.addHeader("Content-Type", "text/plain");
        headers.recycle();
        headers.addHeader("Content-Disposition", "form-data");
        
        assertNull(headers.getContentType());
        assertEquals("form-data", headers.getContentDisposition());
    }
}