package org.dochi.http.request;

import org.dochi.http.request.data.HttpMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpMethodTest {
    @Test
    void method() {
        assertEquals("GET", HttpMethod.GET.getMethod());
        assertEquals("POST", HttpMethod.POST.getMethod());
        assertEquals("PATCH", HttpMethod.PATCH.getMethod());
        assertEquals("PUT", HttpMethod.PUT.getMethod());
        assertEquals("DELETE", HttpMethod.DELETE.getMethod());
    }
}


