package org.dochi.internal;

public class Headers {
    private static final int DEFAULT_HEADER_FIELD_COUNT = 8;
    private int len = 0;
    private int count = 0;

    private HeaderField[] headers;
    
    public Headers() {
        initHeaders(DEFAULT_HEADER_FIELD_COUNT);
    }

    public Headers(int length) {
        initHeaders(length);
    }

    private void initHeaders(int len) {
        this.headers = new HeaderField[len];
        // 배열의 각 요소 초기화
        for (int i = 0; i < this.headers.length; i++) {
            this.headers[i] = new HeaderField();
        }
        this.len = len;
    }

    public void recycle() {
        for(int i = 0; i < this.count; ++i) {
            this.headers[i].recycle();
        }
        this.count = 0;
    }

    public HeaderField createHeader() {
        if (this.count >= this.len) {
            int newLength = this.count * 2;
            if (this.len > 0 && newLength > this.len) {
                this.len = newLength;
            }
            HeaderField[] tmp = new HeaderField[len];
            System.arraycopy(this.headers, 0, tmp, 0, count);
            for (int i = count; i < len; i++) {
                tmp[i] = new HeaderField();
            }
            this.headers = tmp;
        }
        return headers[count++];
    }

    public int size() {
        return this.count;
    }

    public HeaderBytes getValue(String name) {
        for(int i = 0; i < this.count; i++) {
            if (this.headers[i].name().equalsIgnoreCase(name)) {
                return this.headers[i].getValue();
            }
        }
        return null;
    }

    public String getHeader(String name) {
        HeaderBytes byteValue = this.getValue(name);
        return byteValue != null ? byteValue.toString() : null;
    }
}
