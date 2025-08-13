package org.dochi.http.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class Http11ParserTest {
    void parseQueryString_correct_input() {
        String queryString = "userId=john&password=1234&name=john park";

        Map<String, String> expected = Map.of(
                "userId", "john",
                "password", "1234",
                "name", "john park"
        );

        Map<String, String> result = HttpParser.parseQueryString(queryString);

        assertEquals(expected, result);
    }

    @Test
    void parseQueryString_parameter_separator_duplication_sequence() {
        String queryString = "userId=john&password==1234&name=john park";

        Map<String, String> expected = Map.of(
                "userId", "john",
                "name", "john park"
        );

        Map<String, String> result = HttpParser.parseQueryString(queryString);

        assertEquals(expected, result);
    }

    @Test
    void parseQueryString_parameter_separator_duplication_disorder() {
        String queryString = "userId=john&pas=sword=1234&name=john park";

        Map<String, String> expected = Map.of(
                "userId", "john",
                "name", "john park"
        );

        Map<String, String> result = HttpParser.parseQueryString(queryString);

        assertEquals(expected, result);
    }

    @Test
    void parseQueryString_non_key() {
        String queryString = "userId=john&password=1234&=john park";

        Map<String, String> expected = Map.of(
                "userId", "john",
                "password", "1234"
        );

        Map<String, String> actual = HttpParser.parseQueryString(queryString);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseQueryString_non_value() {
        String queryString = "userId=john&password=1234&name=";

        Map<String, String> expected = Map.of(
                "userId", "john",
                "password", "1234",
                "name", ""
        );

        Map<String, String> actual = HttpParser.parseQueryString(queryString);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseRequestUri_correct_input() {
        // given
        String requestUri = "/user/create?key=value";

        // when
        HttpParser.Pair actual = HttpParser.parseRequestUri(requestUri);

        // then
        HttpParser.Pair expected = new HttpParser.Pair("/user/create", "key=value");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseRequestUri_separator_duplication() {
        // given
        String requestUri = "key??value";

        // when
        HttpParser.Pair actual = HttpParser.parseRequestUri(requestUri);

        // then
        HttpParser.Pair expected = new HttpParser.Pair("key??value", "");;
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseRequestUri_including_space() {
        // given
        String requestUri = "/user/crea te?ke y=val ue";

        // when
        HttpParser.Pair actual = HttpParser.parseRequestUri(requestUri);

        // then
        HttpParser.Pair expected = new HttpParser.Pair("/user/crea te", "ke y=val ue");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseRequestUri_non_querystring() {
        // given
        String requestUri = "/";

        // when
        HttpParser.Pair actual = HttpParser.parseRequestUri(requestUri);

        // then
        HttpParser.Pair expected = new HttpParser.Pair("/", "");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseRequestLine_correct_input() {
        String requestLine = "GET / HTTP/1.1";

        String[] tokens = HttpParser.parseRequestLine(requestLine);

        String[] expected = new String[]{"GET", "/", "HTTP/1.1"};

        assertThat(tokens).isEqualTo(expected);
    }

    @Test
    void parseRequestLine_incorrect_input() {
        String requestLine = "GET / ";

        assertThrows(IllegalArgumentException.class, () -> HttpParser.parseRequestLine(requestLine));
    }

    @Test
    void parseHeader_Line_correct_input() {
        String header = "Host: www.example.com";

        HttpParser.Pair actual = HttpParser.parseHeaderLine(header);

        HttpParser.Pair expected = new HttpParser.Pair("Host", "www.example.com");

        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void parseHeader_Line_non_value() {
        String header = "Host";

        HttpParser.Pair actual = HttpParser.parseHeaderLine(header);

        HttpParser.Pair expected = new HttpParser.Pair("Host", "");

        assertThat(actual).isEqualTo(expected);
    }
}