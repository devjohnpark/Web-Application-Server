package org.dochi.webserver;

import org.dochi.webserver.attribute.WebServer;
import org.dochi.webserver.executor.ServerExecutor;

public class WebServerLauncher {
    public static void main(String[] args) {
        WebServer webServer1 = new WebServer(8080, "0.0.0.0");
//        webServer1.getConfig().getWebService().setWebResourceRootPath("static-resources");


        webServer1.getConfig().getKeepAlive().setKeepAliveTimeout(5000);
        webServer1.getConfig().getKeepAlive().setMaxKeepAliveRequests(50);
        webServer1.getConfig().getThreadPool().setMinSpareThreads(1000);
        webServer1.getConfig().getThreadPool().setMaxThreads(2000);

//        WebServer webServer2 = new WebServer(7070, "localhost");
//        webServer2.getConfig().getHttpReqAttribute().setRequestHeaderMaxSize(16000);

        ServerExecutor.addWebServer(webServer1);
//        ServerExecutor.addWebServer(webServer2);

        ServerExecutor.execute();
    }
}