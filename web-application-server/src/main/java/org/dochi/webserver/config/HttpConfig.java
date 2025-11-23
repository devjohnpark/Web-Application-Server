package org.dochi.webserver.config;

import org.dochi.webserver.property.HttpRequestProperty;
import org.dochi.webserver.property.HttpResponseProperty;

public interface HttpConfig {
    HttpRequestProperty getReqConfig();
    HttpResponseProperty getResConfig();
}
