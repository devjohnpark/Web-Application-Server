package org.dochi.connector;

import org.dochi.internal.Request;
import org.dochi.external.HttpExternalRequest;
import org.dochi.internal.buffer.InputBuffer;

public interface RequestHandler extends HttpExternalRequest {
    void setInputBuffer(InputBuffer inputBuffer);
    void recycle();
    Request getRequest();
}

