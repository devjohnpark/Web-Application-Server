package org.dochi.webserver.property;

import org.dochi.webresource.WebResourceProvider;
import org.dochi.webserver.config.WebServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServiceProperty implements WebServiceConfig {
    private static final Logger log = LoggerFactory.getLogger(WebServiceProperty.class);
    private WebResourceProvider webResourceProvider;

    public void setWebResourceProvider(WebResourceProvider webResourceProvider) {
        this.webResourceProvider = webResourceProvider;
    }

    public WebResourceProvider getWebResourceProvider() {
        return webResourceProvider;
    }
}
