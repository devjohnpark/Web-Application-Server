package org.dochi.webresource;

public class Resource {
    private byte[] data = null;
    private String mimeType = null;

    public Resource() {}

    public Resource(byte[] data, String mimeType) {
        this.data = data;
        this.mimeType = mimeType;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType(String parameter) {
        return ResourceType.fromMimeType(mimeType).getContentType(parameter);
    }

    public boolean isEmpty() {
        return data == null;
    }
}