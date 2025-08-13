package org.dochi.http.request.multipart;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Multipart {
    private final Map<String, Part> parts = new HashMap<>();

    public boolean isLoad() {
        return !parts.isEmpty();
    }

//    public Map<String, Part> getParts() {
//        return parts;
//    }

    public void addPart(String name, Part part) {
        this.parts.put(name, part);
    }

    public Part getPart(String name) {
        if (parts.isEmpty()) {
            return new Part();
        }
        return parts.get(name);
    }

    public void recycle() throws IOException {
        if (parts.isEmpty()) {
            return;
        }
        clearParts();
    }

    private void clearParts() throws IOException {
        Set<String> keys = parts.keySet();
        for (String key: keys) {
            parts.get(key).removeFile();
        }
        parts.clear();
    }
}
