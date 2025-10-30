package org.dochi.internal.buffer;

public class MimeHeaderField {
    private final HeaderBytes nameMB = HeaderBytes.newInstance();
    private final HeaderBytes valueMB = HeaderBytes.newInstance();

    public HeaderBytes name() {
        return nameMB;
    }

    public HeaderBytes getValue() {
        return valueMB;
    }

    public void recycle() {
        this.nameMB.recycle();
        this.valueMB.recycle();
    }

    public String toString() {
        return String.valueOf(this.nameMB) + ": " + String.valueOf(this.valueMB);
    }
}
