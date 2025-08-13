package org.dochi.http.response;

import org.dochi.webresource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class ResponseHeadersTest {

    private ResponseHeaders headers;

    @BeforeEach
    void setUp() {
        headers = new ResponseHeaders();
    }

    @Test
    void add_get_headers() {
        headers.addHeader(ResponseHeaders.CONTENT_TYPE, ResourceType.HTML.getContentType("UTF-8"));
        headers.addHeader(ResponseHeaders.CONTENT_LENGTH, "10");

        assertThat(headers.getHeaders().get(ResponseHeaders.CONTENT_TYPE)).isEqualTo(ResourceType.HTML.getContentType("UTF-8"));
        assertThat(headers.getHeaders().get(ResponseHeaders.CONTENT_LENGTH)).isEqualTo("10");
        assertThat(headers.getHeaders().size()).isEqualTo(2);
    }

    @Test
    void addHeader_key_null() {
        headers.addHeader(null, ResourceType.HTML.getContentType("UTF-8"));

        assertTrue(headers.getHeaders().isEmpty());
    }

    @Test
    void addHeader_value_null() {
        headers.addHeader(ResponseHeaders.CONTENT_TYPE, null);

        assertTrue(headers.getHeaders().isEmpty());
    }

    @Test
    void addHeader_key_empty() {
        headers.addHeader("", ResourceType.HTML.getContentType("UTF-8"));

        assertTrue(headers.getHeaders().isEmpty());
    }

    @Test
    void addHeader_value_empty() {
        headers.addHeader(ResponseHeaders.CONTENT_TYPE, "");

        assertThat(headers.getHeaders().get(ResponseHeaders.CONTENT_TYPE)).isEqualTo("");
    }
}