package org.dochi.webserver.lifecycle;

import org.dochi.webserver.socket.Connector;
import org.dochi.webserver.attribute.WebServer;
import org.dochi.webserver.socket.SocketTaskExecutorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class ServerLifecycle extends LifecycleBase {
    private static final Logger log = LoggerFactory.getLogger(ServerLifecycle.class);

    private final WebServer webServer;

    public ServerLifecycle(WebServer webServer) {
        this.webServer = webServer;
        this.addLifeCycle(new WebServiceLifecycle(webServer.getConfig().getWebService()));
    }

    public void start() throws LifecycleException {
        log.info("Starting server...");
        super.start();
        log.info("ServerLifeCycle started");
        logStartedWebServer(webServer);
        try(ServerSocket serverSocket = new ServerSocket()) {
            Connector connector = new Connector(serverSocket);
            connector.connect(
                    SocketTaskExecutorFactory.getInstance().createExecutor(webServer.getConfig()),
                    webServer.getHostName(),
                    webServer.getPort()
            );
        } catch (IOException e) {
            logAcceptError(webServer, e);
        }
        log.info("try-with-resources: Socket Closed.");
    }

    public void stop() throws LifecycleException {
        super.stop();
        log.info("Web server stopped [Host: {}, Port: {}]", webServer.getHostName(), webServer.getPort());
    }

    private void logStartedWebServer(WebServer webServer) {
        log.info("Web server started [Host: {}, Port: {}]", webServer.getHostName(), webServer.getPort());
    }

    private void logAcceptError(WebServer webServer, IOException e) {
        log.error("Server socket accept error: {} [Host: {}, Port: {}]", webServer.getHostName(), webServer.getPort(), e.getMessage());
    }
}
