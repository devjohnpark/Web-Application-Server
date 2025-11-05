package org.dochi.webserver.lifecycle;

import org.dochi.api.handler.HttpApiHandler;
import org.dochi.webserver.attribute.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WebServiceLifecycle implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(WebServiceLifecycle.class);
    private final WebService webService;

    public WebServiceLifecycle(WebService webService) {
        this.webService = webService;
    }

    @Override
    public void start() throws LifecycleException {

    }

    @Override
    public void stop() throws LifecycleException {

    }

    @Override
    public void init() throws LifecycleException {
        for (HttpApiHandler service: webService.getServices().values()) {
            service.init(webService.getServiceConfig());
        }
        log.info("{} initialized", webService.getClass().getSimpleName());
    }

    @Override
    public void destroy() throws LifecycleException {
        for (HttpApiHandler service: webService.getServices().values()) {
            service.destroy();
        }
        log.info("{} destroyed", webService.getClass().getSimpleName());
    }
}
