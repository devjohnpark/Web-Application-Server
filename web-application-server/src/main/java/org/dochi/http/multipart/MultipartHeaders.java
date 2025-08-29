package org.dochi.http.multipart;

import java.util.HashMap;
import java.util.Map;

import static org.dochi.http.utils.HttpParser.*;

public class MultipartHeaders {
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_TYPE = "Content-Type";

    private final Map<String, String> headers = new HashMap<>();

    public void addHeader(String line) {
        if (line == null || line.isEmpty()) {
            return;
        }
        Pair pair = parseHeaderLine(line);
        headers.put(pair.key().toLowerCase(), pair.value());
    }

    public void addHeader(String name, String value) {
        if (name == null || value == null || name.isEmpty() || value.isEmpty()) {
            return;
        }
        headers.put(name.toLowerCase(), value);
    }


    public String getHeader(String key) {
        return headers.get(key.toLowerCase());
    }

    public String getContentDisposition() {
        return getHeader("content-disposition");
    }

    public String getContentType() {
        return getHeader("content-type");
    }

    public void recycle() {
        if (!headers.isEmpty()) {
            headers.clear();
        }
    }
}
