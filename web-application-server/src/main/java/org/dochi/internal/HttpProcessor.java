package org.dochi.internal;

import org.dochi.net.AbstractEndpoint.Handler.SocketState;
import org.dochi.net.AbstractSocketWrapper;

public interface HttpProcessor {
     SocketState process(AbstractSocketWrapper<?> socketWrapper);
     void recycle();
}
