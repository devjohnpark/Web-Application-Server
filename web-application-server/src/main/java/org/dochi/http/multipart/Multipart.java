package org.dochi.http.multipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Multipart {
    private static final Logger log = LoggerFactory.getLogger(Multipart.class);
    private final Map<String, Part> parts = new HashMap<>();

    public void addPart(String name, Part part) {
        this.parts.put(name, part);
    }

    public Part getPart(String name) {
        Part part = parts.get(name);
        if (part == null) {
            return new Part();
        }
        return part;
    }

    public void recycle() {
        if (parts.isEmpty()) {
            return;
        }
        clearParts();
    }

    private void clearParts() {
        Set<String> keys = parts.keySet();
        for (String key: keys) {
            try {
                parts.get(key).removeFile();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        parts.clear();
    }
}
