package org.dochi.webserver.lifecycle;

public interface Lifecycle {

    default void start() throws LifecycleException { }

    default void stop() throws LifecycleException { }

    void init() throws LifecycleException;
    void destroy() throws LifecycleException;
}
