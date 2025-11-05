package org.dochi.internal;

public class HeaderField {
    private final HeaderBytes name = new HeaderBytes();
    private final HeaderBytes value = new HeaderBytes();

    public HeaderBytes name() {
        return name;
    }

    public HeaderBytes getValue() {
        return value;
    }

    public void recycle() {
        this.name.recycle();
        this.value.recycle();
    }

    public String toString() {
        return String.valueOf(this.name) + ": " + String.valueOf(this.value);
    }
}
