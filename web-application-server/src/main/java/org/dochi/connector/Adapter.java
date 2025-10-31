package org.dochi.connector;

import org.dochi.internal.request.Request;
import org.dochi.internal.response.Response;

import java.io.IOException;

public interface Adapter {
    void service(Request request, Response response) throws IOException;
}
