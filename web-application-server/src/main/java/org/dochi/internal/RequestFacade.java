package org.dochi.internal;

public abstract class RequestFacade implements RequestContext {
    protected final Request request;

    public RequestFacade(Request request) {
        this.request = request;
    }
}
