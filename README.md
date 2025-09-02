# Implementation of an HTTP Web Server
---
## VERSION

- **0.0.0: Initial WAS Implementation**
    - Process HTTP/1.1 Request Messages
    - Process HTTP/1.1 Response Messages
    - Handle HTTP API Requests
    - Serve Web Resource Files (HTML, CSS, JS, Images)

- 0.0.1
    - Support Virtual Servers by Port

- 0.0.2
    - Introduce Worker Thread Pool and Socket Operation Object Pool for Efficient HTTP Request Handling
    - Manage Lifecycle of Web Server Instances and Web Service Endpoints
    - Parse HTML Form Data
        - `application/x-www-form-urlencoded`
        - `multipart/form-data`
    - Handle HTTP Status Errors (400, 404, 500, etc.)

- 0.0.3
    - Enable Internal Resource Loading via ClassLoader for Single JAR Deployment

- **0.1.0: HTTP/1.1 Keep-Alive (Persistent Connection)**
    - Refactor Internals to Support Persistent Connections and Reuse Objects
    - Support HTTP/1.1 Keep-Alive with Configurable Timeout and Max Request Count

- **0.2.0: Protocol Decoupling & Performance Optimization**
    - Resolve protocol dependency by separating low-level and high-level processing into a two-layer architecture
    - Achieve over 23x throughput improvement by buffering header in byte[], parsing keys/values by index, and using lazy loading for required field as `String`
    - Leverage Cache Locality with LIFO-based Object Pool
    - Enhance Socket Extensibility with `SocketWrapperBase<E>` for Future NIO
    - Support Virtual Servers by Port and Domain

---

## How to use

#### Single web server
```java
public class WebServerLauncher {  
    public static void main(String[] args) throws IOException {  
        WebServer server = new WebServer();  
        server.getWebService().addService("/user/create", new LoginHttpApiHandler());  
        server.start();  
    }  
}
```

#### Virtual web servers 
```java
public class WebServerLauncher {
    public static void main(String[] args) throws IOException {
        WebServer localServer = new WebServer(8080, "localhost");
        WebServer remoteServer = new WebServer(80, "0.0.0.0");
        remoteServer.getConfig().getWebService().addService("/user/create", new LoginHttpApiHandler())
                                           .addService("/upload", new UploadFileHttpApiHandler());
        ServerExecutor.addWebServer(localServer);
        ServerExecutor.addWebServer(remoteServer);
        ServerExecutor.execute();
    }
}  
```
