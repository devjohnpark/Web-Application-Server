package org.dochi.http.response;

import org.dochi.http.request.data.HttpVersion;

public class StatusLine {
    private HttpVersion version;
    private HttpStatus status;

    public StatusLine(HttpVersion protocol, HttpStatus status) {
        this.version = protocol;
        this.status = status;
    }

    public void setStatus(HttpStatus status) {
        if (status == null) {
            return;
        }
        this.status = status;
    }

    public void setVersion(HttpVersion version) {
        if (version == null) {
            return;
        }
        this.version = version;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("%s %d %s\r\n", version.getVersion(), status.getCode(), status.getMessage());
    }
}
