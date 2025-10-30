package org.dochi.connector;

import org.dochi.internal.RequestHeader;
import org.dochi.internal.buffer.InputBuffer;

public interface InternalRequest {
    void setInputBuffer(InputBuffer inputBuffer);
    void recycle();
    RequestHeader getRequestHeader();
}

