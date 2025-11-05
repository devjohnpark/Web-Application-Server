package org.dochi.internal;

import java.io.OutputStream;

public interface ResponseContext {
    void setOutputStream(OutputStream outputStream);
    void recycle();
}
