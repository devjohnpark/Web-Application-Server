package org.dochi.connector;

import org.dochi.internal.Request;
import org.dochi.internal.Response;

import java.io.IOException;

public interface Adapter {
    void service(Request request, Response response) throws IOException;
}
