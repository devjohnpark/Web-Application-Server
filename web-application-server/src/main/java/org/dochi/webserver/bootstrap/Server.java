package org.dochi.webserver.bootstrap;

import org.dochi.internal.HttpProtocolHandler;
import org.dochi.webserver.lifecycle.Lifecycle;
import org.dochi.webserver.lifecycle.AbstractLifecycle;
import org.dochi.webserver.lifecycle.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends AbstractLifecycle {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private final Thread serverShutdownHook;

    public Server() {
        this.serverShutdownHook = new ServerShutdownHook();
    }

    public void setWebService(WebService webService) {
        this.addLifecycle(0, webService);
    }

    public void setHttpProtocolHandler(HttpProtocolHandler protocolHandler) {
        this.addLifecycle(1, protocolHandler);
    }

    @Override
    protected void initInternal() throws LifecycleException {
        propagateLifecycles(Lifecycle::init);
    }

    @Override
    protected void startInternal() throws LifecycleException {
        ShutdownTasksManager.getInstance().addShutdownHook(serverShutdownHook);
        propagateLifecycles(Lifecycle::start);
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        propagateLifecycles(Lifecycle::stop);
        ShutdownTasksManager.getInstance().removeShutdownHook(serverShutdownHook);
    }

    @Override
    protected void destroyInternal() throws LifecycleException {
        propagateLifecycles(Lifecycle::destroy);
    }

    protected class ServerShutdownHook extends Thread {

        public ServerShutdownHook() {
            setName("server-shutdown-hook");
            setDaemon(false);
        }

        @Override
        public void run() {
            try {
                Server.this.stop();
            } catch (LifecycleException e) {
                log.error("server shutdown task failed", e);
            }
        }
    }
}
