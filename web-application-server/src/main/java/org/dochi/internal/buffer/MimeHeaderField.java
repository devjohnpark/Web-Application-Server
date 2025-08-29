package org.dochi.internal.buffer;

public class MimeHeaderField {
    private final MessageBytes nameMB = MessageBytes.newInstance();
    private final MessageBytes valueMB = MessageBytes.newInstance();

    public MessageBytes getName() {
        return nameMB;
    }

    public MessageBytes getValue() {
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
