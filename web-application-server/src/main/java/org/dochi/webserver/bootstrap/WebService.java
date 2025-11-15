package org.dochi.webserver.bootstrap;

import org.dochi.api.handler.DefaultHttpApiHandler;
import org.dochi.api.handler.HttpApiHandler;
import org.dochi.webresource.WebResourceProvider;
import org.dochi.webserver.lifecycle.AbstractLifecycle;
import org.dochi.webserver.lifecycle.LifecycleException;
import org.dochi.webserver.property.WebServiceProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WebService extends AbstractLifecycle {
    private static final Logger log = LoggerFactory.getLogger(WebService.class);
    private static final String DEFAULT_ROOT_DIR = "webapp";
    private static final String rootPath = "/";
    private final Map<String, HttpApiHandler> services = new HashMap<>();
    private final WebServiceProperty webServiceProperty = new WebServiceProperty();
    private String rootResourcePath = DEFAULT_ROOT_DIR;

    public WebService() {
        services.put(rootPath, new DefaultHttpApiHandler());
    }

    public void setWebResourceRootPath(String webResourceRootPath) {
        this.rootResourcePath = webResourceRootPath;
    }

    public WebService addService(String path, HttpApiHandler service) {
        services.put(path, service);
        return this;
    }

    public HttpApiHandler getService(String path) {
        return services.get(path);
    }

    public int getSize() {
        return services.size();
    }

    @Override
    protected void initInternal() throws LifecycleException {
        webServiceProperty.setWebResourceProvider(new WebResourceProvider(Path.of(rootResourcePath)));
        for (HttpApiHandler service : services.values()) {
            service.init(webServiceProperty);
        }
    }

    @Override
    protected void startInternal() throws LifecycleException {
        // Nothing
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        // Nothing
    }

    @Override
    protected void destroyInternal() throws LifecycleException {
        for (HttpApiHandler service : services.values()) {
            service.destroy();
        }
    }
}
