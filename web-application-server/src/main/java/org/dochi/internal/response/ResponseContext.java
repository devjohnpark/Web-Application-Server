package org.dochi.internal.response;

import java.io.OutputStream;

public interface ResponseContext {
    void setOutputStream(OutputStream outputStream);
    void recycle();
}
