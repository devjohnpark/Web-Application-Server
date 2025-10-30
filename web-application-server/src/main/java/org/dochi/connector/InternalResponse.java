package org.dochi.connector;

import java.io.IOException;

public interface InternalResponse {
    void setOutputStream(TmpBufferedOutputStream outputStream);
    void recycle();
    void flush() throws IOException;
}
