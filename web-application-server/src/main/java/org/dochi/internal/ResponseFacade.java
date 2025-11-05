package org.dochi.internal;

public abstract class ResponseFacade implements ResponseContext {
    protected final Response response;

    public ResponseFacade(Response response) {
        this.response = response;
    }
}
