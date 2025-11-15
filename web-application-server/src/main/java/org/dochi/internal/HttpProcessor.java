package org.dochi.internal;

import org.dochi.net.AbstractEndpoint.Handler.SocketState;
import org.dochi.net.AbstractSocketWrapper;

import java.io.IOException;

public interface HttpProcessor {
     SocketState process(AbstractSocketWrapper<?> socketWrapper) throws IOException;
     void recycle();
}
