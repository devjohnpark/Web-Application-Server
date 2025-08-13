package org.dochi.webserver.attribute;

import org.dochi.webserver.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private final int port;
    private final String hostName;
    private final ServerConfig serverConfig = new ServerConfig();

    public WebServer() {
        this(8080, "localhost");
    }

    public WebServer(int port) {
        this(port, "localhost");
    }

    public WebServer(int port, String hostName) {
        this.port = port;
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public ServerConfig getConfig() { return serverConfig; }
}
