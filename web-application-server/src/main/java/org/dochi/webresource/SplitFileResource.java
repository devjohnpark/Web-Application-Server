package org.dochi.webresource;

import java.io.InputStream;

public class SplitFileResource {
    private InputStream in = null;
    private long fileSize = 0;
    private String mimeType = null;

    public SplitFileResource() {}

    public SplitFileResource(InputStream in, long fileSize, String mimeType) {
        this.in = in;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    public InputStream getInputStream() {
        return in;
    }

    public String getContentType(String parameter) {
        return ResourceType.fromMimeType(mimeType).getContentType(parameter);
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isEmpty() {
        return in == null;
    }
}
