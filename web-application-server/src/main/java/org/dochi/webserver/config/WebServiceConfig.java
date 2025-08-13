package org.dochi.webserver.config;

import org.dochi.webresource.WebResourceProvider;
import org.dochi.webserver.attribute.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServiceConfig {
    private static final Logger log = LoggerFactory.getLogger(WebServiceConfig.class);
    private final WebResourceProvider webResourceProvider;

    public WebServiceConfig(WebResourceProvider webResourceProvider) {
        this.webResourceProvider = webResourceProvider;
    }

    public WebResourceProvider getWebResourceProvider() {
        return webResourceProvider;
    }
}
