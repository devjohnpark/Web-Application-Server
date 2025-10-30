package org.dochi.internal.buffer;

public class HeaderField {
    private final HeaderBytes nameMB = new HeaderBytes();
    private final HeaderBytes valueMB = new HeaderBytes();

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
