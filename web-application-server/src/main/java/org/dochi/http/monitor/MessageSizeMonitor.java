package org.dochi.http.monitor;

public interface MessageSizeMonitor {
    int getSizeLimit();

    void monitorSize(int size);
}
