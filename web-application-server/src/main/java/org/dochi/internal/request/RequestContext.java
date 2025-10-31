package org.dochi.internal.request;

import org.dochi.internal.buffer.InputBuffer;

public interface RequestContext {
    void setInputBuffer(InputBuffer inputBuffer);
    void recycle();
}
