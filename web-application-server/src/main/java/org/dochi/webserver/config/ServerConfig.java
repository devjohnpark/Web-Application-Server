package org.dochi.webserver.config;

import org.dochi.webserver.attribute.*;

public class ServerConfig {
    private final KeepAlive keepAlive = new KeepAlive();
    private final WebService webService = new WebService();
    private final ThreadPool threadPool = new ThreadPool();
    private final HttpReqAttribute httpReqAttribute = new HttpReqAttribute();
    private final HttpResAttribute httpResAttribute = new HttpResAttribute();

    public WebService getWebService() { return webService; }

    public KeepAlive getKeepAlive() { return keepAlive; }

    public ThreadPool getThreadPool() { return threadPool; }

    public HttpReqAttribute getHttpReqAttribute() { return httpReqAttribute; }

    public HttpResAttribute getHttpResAttribute() { return httpResAttribute; }
}
