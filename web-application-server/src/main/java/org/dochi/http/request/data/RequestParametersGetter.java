package org.dochi.http.request.data;

import java.io.IOException;

public interface RequestParametersGetter {
    String getRequestParameterValue(String key) throws IOException;
}
