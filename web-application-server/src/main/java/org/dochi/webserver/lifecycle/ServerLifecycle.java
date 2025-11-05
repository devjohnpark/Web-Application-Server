package org.dochi.webserver.lifecycle;

import org.dochi.connector.Connector;
import org.dochi.connector.InternalAdapter;
import org.dochi.webserver.net.BioEndpoint;
import org.dochi.webserver.config.HttpConfigImpl;
import org.dochi.internal.HttpProtocolHandler;
import org.dochi.webserver.net.Acceptor;
import org.dochi.webserver.attribute.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLifecycle extends LifecycleBase {
    private static final Logger log = LoggerFactory.getLogger(ServerLifecycle.class);
    private final WebServer webServer;

    public ServerLifecycle(WebServer webServer) {
        this.webServer = webServer;
        this.addLifeCycle(new WebServiceLifecycle(webServer.getConfig().getWebService()));
        HttpProtocolHandler handler = new HttpProtocolHandler(new BioEndpoint(webServer.getPort(), webServer.getHostName()));
        handler.setAdapter(
                new InternalAdapter(
                        new Connector(webServer.getConfig().getWebService(), new HttpConfigImpl(webServer.getConfig().getHttpReqAttribute(), webServer.getConfig().getHttpResAttribute())))
        );
        handler.setHttpConfig(new HttpConfigImpl(webServer.getConfig().getHttpReqAttribute(), webServer.getConfig().getHttpResAttribute()));
        this.addLifeCycle(handler);
    }

    @Override
    public void start() throws LifecycleException {
        log.info("Starting server...");
        super.start();
        log.info("ServerLifeCycle started [Host: {}, Port: {}]",
                webServer.getHostName(), webServer.getPort());
    }

    @Override
    public void stop() throws LifecycleException {
        log.info("Stopping server...");
        super.stop();
        log.info("ServerLifeCycle stopped [Host: {}, Port: {}]",
                webServer.getHostName(), webServer.getPort());
    }
}
