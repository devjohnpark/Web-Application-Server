package org.dochi.webserver.lifecycle;

public interface Lifecycle {
    void start() throws LifecycleException;
    void stop() throws LifecycleException;
    void init() throws LifecycleException;
    void destroy() throws LifecycleException;
}
