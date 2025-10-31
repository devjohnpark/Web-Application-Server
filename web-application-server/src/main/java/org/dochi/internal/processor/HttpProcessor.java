package org.dochi.internal.processor;

//import org.dochi.http.api.InternalAdapter;
//import org.dochi.http.api.mapper.InternalAdapter;
import org.dochi.webserver.socket.SocketWrapper;
import org.dochi.webserver.socket.SocketState;

public interface HttpProcessor {
    SocketState process(SocketWrapper<?> socketWrapper);
}
