package org.dochi.webserver.lifecycle;

public final class LifecycleException extends Exception {

    public LifecycleException(String message) {
        super(message);
    }

    public LifecycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
