package org.dochi.http.processor;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.webserver.socket.SocketState;
import org.dochi.webserver.socket.SocketWrapper;

public interface HttpProcessor {
    SocketState process(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper);
}
