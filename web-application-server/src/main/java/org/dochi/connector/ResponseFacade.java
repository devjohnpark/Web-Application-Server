package org.dochi.connector;

import org.dochi.internal.Response;
import org.dochi.internal.ResponseContext;

import java.io.OutputStream;

public abstract class ResponseFacade implements ResponseContext {
    protected final org.dochi.internal.Response response;
    protected OutputStream out;

    public ResponseFacade(Response response) {
        this.response = response;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void recycle() {
        // not completed this obj, will write the code
    }
}
