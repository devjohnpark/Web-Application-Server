package org.dochi.http.data;

public enum HttpVersion {
    HTTP_0_9("HTTP/0.9"),
    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1");

    private final String version;

    HttpVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public static HttpVersion fromString(String version) {
        for (HttpVersion httpVersion: HttpVersion.values()) {
            if (httpVersion.getVersion().equalsIgnoreCase(version)) {
                return httpVersion;
            }
        }
        throw new IllegalArgumentException("Invalid HTTP version: " + version);
    }
}
