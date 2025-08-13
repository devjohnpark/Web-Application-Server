package org.dochi.http.response.processor;

//import org.dochi.buffer.Http11ResponseProcessor;
import org.dochi.http.api.HttpApiResponse;

import java.io.IOException;

public interface HttpResponseProcessor extends HttpApiResponse {
    void recycle();
    void flush() throws IOException;
}
