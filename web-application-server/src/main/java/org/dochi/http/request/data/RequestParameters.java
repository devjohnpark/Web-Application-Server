package org.dochi.http.request.data;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.dochi.http.util.HttpParser.*;

public class RequestParameters implements RequestParametersGetter {
    private final Map<String, String> parameters = new HashMap<String, String>();

    private void addParameters(String queryString) {
        if (queryString == null || queryString.isEmpty()) { return; }
        parameters.putAll(parseQueryString(queryString));
    }

    public void addRequestParameters(String queryString) {
        addParameters(setQueryString(queryString));
    }

    private String setQueryString(String queryString) {
        return queryString != null ? URLDecoder.decode(queryString, StandardCharsets.UTF_8) : null;
    }

    public String getRequestParameterValue(String key) { return parameters.get(key); }

    public void clear() {
        if (parameters.isEmpty()) {
            return;
        }
        parameters.clear();
    }
}
