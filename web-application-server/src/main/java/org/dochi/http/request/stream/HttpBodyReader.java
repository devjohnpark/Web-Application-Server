package org.dochi.http.request.stream;

import org.dochi.http.monitor.MessageSizeMonitor;

import java.io.IOException;

public interface HttpBodyReader {
    byte[] readAllBody(int contentLength, MessageSizeMonitor sizeMonitor) throws IOException;
}

