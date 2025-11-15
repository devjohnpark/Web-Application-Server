package org.dochi.webserver.bootstrap;

import org.dochi.connector.InternalAdapter;
import org.dochi.internal.HttpProtocolHandler;
import org.dochi.net.BioEndpoint;
import org.dochi.webserver.lifecycle.LifecycleException;
import org.dochi.webserver.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebAppServer {
    private static final Logger log = LoggerFactory.getLogger(WebAppServer.class);
    private Server server;
    private final int port;
    private final String hostName;
    private final WebService webService = new WebService();
    private final SocketProperty socket = new SocketProperty();
    private final ThreadPoolProperty threadPool = new ThreadPoolProperty();
    private final HttpProperty http = new HttpProperty(new HttpRequestProperty(), new HttpResponseProperty());

    public WebAppServer() {
        this(8080, "localhost");
    }

    public WebAppServer(int port) {
        this(port, "localhost");
    }

    public WebAppServer(int port, String hostName) {
        this.port = port;
        this.hostName = hostName;
    }

    public String getHostName() { return hostName; }

    public int getPort() { return port; }

    public WebService getWebService() { return webService; }

    public SocketProperty getSocket() { return socket; }

    public ThreadPoolProperty getThreadPool() { return threadPool; }

    public HttpProperty getHttp() { return http; }

    public Server getServer() {
        if (server == null) {
            server = new Server();
            server.setWebService(webService);
            HttpProtocolHandler httpProtocolHandler = new HttpProtocolHandler(new BioEndpoint(port, hostName));
            httpProtocolHandler.setAdapter(new InternalAdapter(webService, http));
            httpProtocolHandler.setHttpConfig(http);
            httpProtocolHandler.setSocketConfig(socket);
            httpProtocolHandler.setThreadPoolConfig(threadPool);
            server.setHttpProtocolHandler(httpProtocolHandler);
        }
        return server;
    }

    public void start() throws LifecycleException {
       getServer().start();
    }

    public void stop() throws LifecycleException {
        getServer().stop();
    }
}
