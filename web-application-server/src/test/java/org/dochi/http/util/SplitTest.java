package org.dochi.http.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SplitTest {
    @Test
    void split_empty() {
        String str = "";
        String[] tokens = str.split("");
        assertThat(tokens.length).isEqualTo(1);
        assertThat(tokens[0]).isEqualTo("");
    }

    @Test
    void split_max_limit() {
        String str = "key=value=etc";
        String[] tokens = str.split("=", 2);
        assertThat(tokens.length).isEqualTo(2);
        assertThat(tokens[0]).isEqualTo("key");
        assertThat(tokens[1]).isEqualTo("value=etc");
    }

    @Test
    void split_other_separator() {
        String str = "=user";
        String[] tokens = str.split("&");
        assertThat(tokens.length).isEqualTo(1);
        assertThat(tokens[0]).isEqualTo("=user");
    }

    @Test
    void split_left_separator() {
        String str = "=user";
        String[] tokens = str.split("=");
        assertThat(tokens.length).isEqualTo(2);
        assertThat(tokens[0]).isEqualTo("");
        assertThat(tokens[1]).isEqualTo("user");
    }

    @Test
    void split_right_separator() {
        String str = "user=";
        String[] tokens = str.split("=");
        assertThat(tokens.length).isEqualTo(1);
        assertThat(tokens[0]).isEqualTo("user");
    }

    @Test
    void split_limit() {
        String str = "user=john=park";
        String[] tokens = str.split("=", 2);
        assertThat(tokens.length).isEqualTo(2);
        assertThat(tokens[1]).isEqualTo("john=park");
    }
}
