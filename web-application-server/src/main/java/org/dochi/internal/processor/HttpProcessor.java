package org.dochi.internal.processor;

//import org.dochi.http.api.InternalAdapter;
//import org.dochi.http.api.mapper.HttpMapper;
import org.dochi.webserver.socket.SocketWrapper;
import org.dochi.webserver.socket.SocketState;

public interface HttpProcessor {
    SocketState process(SocketWrapper<?> socketWrapper);
}
