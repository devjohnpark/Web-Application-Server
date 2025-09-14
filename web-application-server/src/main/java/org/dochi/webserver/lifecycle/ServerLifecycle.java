package org.dochi.webserver.lifecycle;

import org.dochi.webserver.socket.Connector;
import org.dochi.webserver.attribute.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerLifecycle extends LifecycleBase {
    private static final Logger log = LoggerFactory.getLogger(ServerLifecycle.class);
    private final WebServer webServer;
    private Connector connector;
    private Thread acceptThread;

    public ServerLifecycle(WebServer webServer) {
        this.webServer = webServer;
        this.addLifeCycle(new WebServiceLifecycle(webServer.getConfig().getWebService()));
    }

    @Override
    public void start() throws LifecycleException {
        log.info("Starting server...");
        super.start();
        try {
            this.connector = new Connector(
                    new ServerSocket(),
                    webServer.getConfig()
            );
            this.connector.bind(webServer.getHostName(), webServer.getPort());

            this.acceptThread = new Thread(connector, "acceptor");
            this.acceptThread.start();

            log.info("ServerLifeCycle started [Host: {}, Port: {}]",
                    webServer.getHostName(), webServer.getPort());
        } catch (IOException e) {
            throw new LifecycleException("Failed to start connector", e);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        super.stop();
        try {
            if (connector != null) connector.close();
            if (acceptThread != null) acceptThread.join(3000);
        } catch (Exception e) {
            throw new LifecycleException("Failed to stop connector", e);
        } finally {
            log.info("Web server stopped [Host: {}, Port: {}]",
                    webServer.getHostName(), webServer.getPort());
        }
    }
}
