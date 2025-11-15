package org.dochi.net;

public abstract class AbstractSocketTask<S> implements Runnable {

    protected AbstractSocketWrapper<S> socketWrapper;

    public AbstractSocketTask(AbstractSocketWrapper<S> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }

    @Override
    public final void run() {
        if (!socketWrapper.isClosed()) {
            doRun();
        }
    }

    protected void reset(AbstractSocketWrapper<S> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }

    protected abstract void doRun();
}
