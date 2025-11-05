package org.dochi.internal;

import org.dochi.internal.buffer.InputBuffer;

public interface RequestContext {
    void setInputBuffer(InputBuffer inputBuffer);
    void recycle();
}
