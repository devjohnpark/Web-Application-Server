package org.dochi.connector;

import org.dochi.external.HttpExternalResponse;

import java.io.IOException;

public interface ResponseHandler extends HttpExternalResponse {
    void setOutputStream(TmpBufferedOutputStream outputStream);
    void recycle();
    void flush() throws IOException;
}
