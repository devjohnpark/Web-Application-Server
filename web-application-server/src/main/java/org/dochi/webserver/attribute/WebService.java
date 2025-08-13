package org.dochi.webserver.attribute;

import org.dochi.http.api.DefaultHttpApiHandler;
import org.dochi.http.api.HttpApiHandler;
import org.dochi.webresource.WebResourceProvider;
import org.dochi.webserver.config.WebServiceConfig;
import org.dochi.webserver.lifecycle.ServerLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WebService {
    private static final Logger log = LoggerFactory.getLogger(WebService.class);
    private static final Path DEFAULT_ROOT_DIR = Path.of("webapp");
    private static final String rootPath = "/";

    private final Map<String, HttpApiHandler> services = new HashMap<>();
    private Path rootResourcePath = DEFAULT_ROOT_DIR;
    private WebServiceConfig webServiceConfig = null;

    public WebService() {
        services.put(rootPath, new DefaultHttpApiHandler());
    }

    public void setWebResourceRootPath(String webResourceRootPath) {
        this.rootResourcePath = Path.of(webResourceRootPath);
    }

    public WebService addService(String path, HttpApiHandler service) {
        services.put(path, service);
        return this;
    }

    public Map<String, HttpApiHandler> getServices() {
        return services;
    }

    public WebServiceConfig getServiceConfig() {
        if (webServiceConfig == null) {
            webServiceConfig = new WebServiceConfig(new WebResourceProvider(rootResourcePath));
        }
        return webServiceConfig;
    }
}
