package org.dochi.connector;

import org.dochi.internal.Request;
import org.dochi.internal.RequestLifecycle;

public abstract class RequestFacade implements RequestLifecycle {
    protected final Request request;
    protected final InputBuffer inputBuffer;

    public RequestFacade(Request request) {
        this.request = request;
        this.inputBuffer = new InputBuffer();
    }

    @Override
    public void init(org.dochi.internal.buffer.InputBuffer inputBuffer) {
        this.inputBuffer.setInputBuffer(inputBuffer);
    }

    @Override
    public void recycle() {
        this.inputBuffer.recycle();
    }
}
