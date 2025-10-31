package org.dochi.webserver;

import org.dochi.webserver.attribute.WebServer;
import org.dochi.webserver.executor.ServerExecutor;

import java.io.IOException;

public class WebServerLauncher {
    public static void main(String[] args) throws IOException {
        WebServer localServer = new WebServer(8080, "localhost");
//        WebServer remoteServer = new WebServer(80, "0.0.0.0");
//        remoteServer.getConfig().getWebService().addService("/user/create", new LoginHttpApiHandler())
//                .addService("/upload", new UploadFileHttpApiHandler());
        ServerExecutor.addWebServer(localServer);
//        ServerExecutor.addWebServer(remoteServer);
        ServerExecutor.execute();
    }
}
