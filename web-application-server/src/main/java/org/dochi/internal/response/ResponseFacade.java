package org.dochi.internal.response;

public abstract class ResponseFacade implements ResponseContext {
    protected final Response response;

    public ResponseFacade(Response response) {
        if (response == null) {
            throw new NullPointerException("response is null");
        }
        this.response = response;
    }
}
