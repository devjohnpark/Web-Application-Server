package org.dochi.internal.request;

public abstract class RequestFacade implements RequestContext {
    protected final Request request;

    public RequestFacade(Request request) {
        if (request == null) {
            throw new NullPointerException("internal.Request cannot be null");
        }
        this.request = request;
    }
}
