package org.dochi.webresource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class WebResourceProviderTest {
    WebResourceProvider webResourceProvider = new WebResourceProvider(Path.of("webapp"));

    @BeforeEach
    void setUp() {
        webResourceProvider = new WebResourceProvider(Path.of("webapp"));
    }

    @Test
    void invalid_root_directory() {
        assertThrows(IllegalArgumentException.class, () -> new WebResourceProvider(null));
        assertThrows(IllegalArgumentException.class, () -> new WebResourceProvider(Path.of("/webapp")));
        assertThrows(IllegalArgumentException.class, () -> new WebResourceProvider(Path.of("/web")));
    }

    @Test
    void getResource() {
        assertThat(webResourceProvider.getResource("index.html").isEmpty()).isEqualTo(false);
        assertThat(webResourceProvider.getResource("/index.html").isEmpty()).isEqualTo(false);
    }

    @Test
    void getResource_non_exist_file() {
        Resource resource = webResourceProvider.getResource("hi.html");
        assertThat(resource.isEmpty()).isEqualTo(true);
    }

    @Test
    void getResource_default_page() {
        Resource resource = webResourceProvider.getResource("/");
        assertThat(resource.isEmpty()).isEqualTo(false);
    }
}