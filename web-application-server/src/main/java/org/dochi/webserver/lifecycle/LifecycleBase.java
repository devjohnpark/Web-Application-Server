package org.dochi.webserver.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class LifecycleBase implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(LifecycleBase.class);
    private final List<Lifecycle> lifecycles = new ArrayList<>();

    protected void addLifeCycle(Lifecycle child) {
        lifecycles.add(child);
    }

    @Override
    public void start() throws LifecycleException {
        for (Lifecycle lifecycle : lifecycles) {
            lifecycle.init();
            lifecycle.start();
        }
    }

    @Override
    public void stop() throws LifecycleException {
        for (Lifecycle lifecycle : lifecycles) {
            lifecycle.destroy();
            lifecycle.stop();
        }
    }

    @Override
    public void init() throws LifecycleException {
        // Nothing by default
    }

    @Override
    public void destroy() throws LifecycleException{
        // Nothing by default
    }
}
