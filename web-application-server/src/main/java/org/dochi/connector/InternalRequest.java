package org.dochi.connector;

import org.dochi.internal.RequestMetadata;
import org.dochi.internal.buffer.InputBuffer;

public interface InternalRequest {
    void setInputBuffer(InputBuffer inputBuffer);
    void recycle();
    RequestMetadata getRequestMetadata();
}

