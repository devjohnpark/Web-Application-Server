package org.dochi.webserver.lifecycle;

public interface Lifecycle {
    void addLifecycle(int index, Lifecycle lifecycle);
    void addLifecycle(Lifecycle lifecycle);
    Lifecycle[] getLifecycles();
    void start() throws LifecycleException;
    void stop() throws LifecycleException;
    void init() throws LifecycleException;
    void destroy() throws LifecycleException;
}
