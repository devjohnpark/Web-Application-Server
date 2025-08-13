package org.dochi.http.request.processor;

import org.dochi.http.api.HttpApiRequest;
import org.dochi.http.request.data.Request;

import java.io.IOException;

// HttpProcessor에서 HttpRequestProcessor을 제어하므로 해당 객체에서만 호출 필요
public interface HttpRequestProcessor extends HttpApiRequest {
    boolean isPrepareHeader() throws IOException;
    void recycle() throws IOException;
    Request getParsedRequest();
}

