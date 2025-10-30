package org.dochi.connector;

import java.io.IOException;
import java.io.OutputStream;

public interface InternalResponse {
    void setOutputStream(OutputStream out);
    void recycle();
    void flush() throws IOException;
}
