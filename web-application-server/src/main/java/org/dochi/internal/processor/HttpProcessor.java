package org.dochi.internal.processor;

//import org.dochi.http.api.InternalAdapter;
//import org.dochi.http.api.mapper.HttpApiMapper;
import org.dochi.api.mapper.HttpApiMapper;
import org.dochi.webserver.socket.SocketWrapperBase;
import org.dochi.webserver.socket.SocketState;

public interface HttpProcessor {
    SocketState process(SocketWrapperBase<?> socketWrapper, HttpApiMapper httpApiMapper);
}
