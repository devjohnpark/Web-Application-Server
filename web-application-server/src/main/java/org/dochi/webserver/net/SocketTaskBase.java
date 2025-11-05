package org.dochi.webserver.net;

public abstract class SocketTaskBase<S> implements Runnable {

    protected SocketWrapperBase<S> socketWrapper;

    public SocketTaskBase(SocketWrapperBase<S> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }

    @Override
    public final void run() {
        if (!socketWrapper.isClosed()) {
            doRun();
        }
    }

    protected void reset(SocketWrapperBase<S> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }

    protected abstract void doRun();
}
