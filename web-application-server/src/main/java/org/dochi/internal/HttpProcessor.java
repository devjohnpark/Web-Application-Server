package org.dochi.internal;

//import org.dochi.http.api.InternalAdapter;
//import org.dochi.http.api.mapper.InternalAdapter;
import org.dochi.webserver.net.EndpointBase.Handler.SocketState;
import org.dochi.webserver.net.SocketWrapperBase;

import java.io.IOException;
//import org.dochi.webserver.socket.SocketState;

public interface HttpProcessor {
     SocketState process(SocketWrapperBase<?> socketWrapper) throws IOException;
     void recycle();
}
