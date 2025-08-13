package org.dochi.http.request;

import org.dochi.http.request.data.HttpVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class HttpVersionTest {

    @Test
    void protocol() {
        HttpVersion httpVersion = HttpVersion.fromString("HTTP/0.9");
        assertThat(httpVersion.getVersion()).isEqualTo("HTTP/0.9");

        httpVersion = HttpVersion.fromString("HTTP/1.0");
        assertThat(httpVersion.getVersion()).isEqualTo("HTTP/1.0");

        httpVersion = HttpVersion.fromString("HTTP/1.1");
        assertThat(httpVersion.getVersion()).isEqualTo("HTTP/1.1");

//        httpVersion = HttpVersion.fromString("HTTP/2.0");
//        assertThat(httpVersion.getVersion()).isEqualTo("HTTP/2.0");
    }

    @Test
    void protocol_exception() {
        assertThrows(IllegalArgumentException.class, () -> HttpVersion.fromString("HTTP/1.2"));
    }

}