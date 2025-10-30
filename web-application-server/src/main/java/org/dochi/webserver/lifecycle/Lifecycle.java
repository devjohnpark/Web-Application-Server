package org.dochi.webserver.lifecycle;

import java.io.IOException;

public interface Lifecycle {

    default void start() throws LifecycleException { }

    default void stop() throws LifecycleException { }

    void init() throws LifecycleException;
    void destroy() throws LifecycleException;
}
