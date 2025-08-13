package org.dochi.http.monitor;

public interface ContentLengthMonitor {
    int getActualContentLength();
    int getMaxContentLength();
}
