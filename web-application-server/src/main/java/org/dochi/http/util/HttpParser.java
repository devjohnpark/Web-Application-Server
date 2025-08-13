package org.dochi.http.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

// Request Line: GET /user/create?userId=john&password=1234&name=john park HTTP/1.1
// url: /user/create?userId=john&password=1234&name=john park
// path: /user/create
// query string: userId=john&password=1234&name=john park
// ?: resource와 query string 간의 분리
// &: query parameter 간의 분리
// =: key, value 간의 분리
public class HttpParser {
    private static final Logger log = LoggerFactory.getLogger(HttpParser.class);

    private HttpParser() {}
    
    public static Map<String, String> parseQueryString(String queryString) {
        return parseValues(queryString, "&");
    }

    public static Map<String, String> parseContentDisposition(String contentDispositionValues) {
        return parseValues(contentDispositionValues, ";");
    }

    private static Map<String, String> parseValues(String values, String separator) {
        if (values == null || values.isEmpty()) {
            return new HashMap<>();
        }
        return parseParameters(values.split(separator));
    }

    private static Map<String, String> parseParameters(String[] parameters) {
        Map<String, String> map = new HashMap<>();
        for (String parameter : parameters) {
            Pair pair = getKeyValue(parameter, "="); // not null
            if (pair != null) {
                map.put(pair.key, pair.value);
            }
        }
        return map;
    }

    public static Pair parseRequestUri(String uri) {
        Pair pair = getKeyValue(uri, "\\?");
        return setEmptyValue(uri, pair);
    }

    // key: can't be empty, value: can be empty
    private static Pair getKeyValue(String keyValue, String separator) {
        if (keyValue == null || keyValue.isEmpty()) {
            return null;
        }
        String[] pair = keyValue.split(separator);
        if (pair.length > 2 || pair[0].isEmpty()) {
            return null;
        }
        if (pair.length > 1) {
            return new Pair(pair[0].trim(), pair[1].trim());
        }
        return new Pair(pair[0].trim(), "");
    }

    public static Pair parseHeaderLine(String headerLine) {
        Pair pair = getKeyValue(headerLine, ":");
        return setEmptyValue(headerLine, pair);
    }

    private static Pair setEmptyValue(String header, Pair pair) {
        if (pair != null) {
            return new Pair(pair.key, pair.value);
        }
        return new Pair(header, "");
    }

    public static String[] parseRequestLine(String requestLine) {
        String[] tokens = requestLine.split(" ");
        if (tokens.length != 3) {
            throw new IllegalArgumentException("Invalid request line: " + requestLine);
        }
        return tokens;
    }

    public record Pair(String key, String value) {}
}

