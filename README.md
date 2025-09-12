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

---

## Mermaid

#### Total

```mermaid
classDiagram
direction TB
	namespace Server_Lifecycle {
        class ServerExecutor {
        }

        class WebServer {
        }

        class ServerLifecycle {
        }

        class Lifecycle {
        }

        class LifecycleBase {
        }

        class WebServiceLifecycle {
        }

        class WebService {
        }

	}
	namespace Socket_Abstraction {
        class SocketWrapperBase {
        }

        class BioSocketWrapper {
        }

	}
	namespace Socket_Task_Execution {
        class Runnable {
        }

        class SocketTask {
        }

        class SocketTaskExecutor {
        }

        class SocketTaskPool {
        }

        class ThreadPoolExecutor {
        }

        class SocketTaskHandler {
        }

	}
	namespace Protocol_Entry {
        class HttpProtocolHandler {
        }

        class HttpProcessorPool {
        }

	}
	namespace Interanl_HTTP_Processing {
        class HttpProcessor {
        }

        class AbstractHttpProcessor {
        }

        class Http11Processor {
        }

        class HttpInputBuffer {
        }

        class Http11InputBuffer {
        }

        class Http11Parser {
        }

        class RequestHandler {
        }

        class ResponseHandler {
        }

        class HttpMapper {
        }

	}
	namespace Connector {
        class HttpRequestHandler {
        }

        class HttpResponseHandler {
        }

	}
	namespace External_API {
        class HttpExternalRequest {
        }

        class HttpExternalResponse {
        }

        class WebResourceProvider {
        }

        class HttpApiHandler {
        }

        class AbstractHttpApiHandler {
        }

	}

	<<interface>> Lifecycle
	<<abstract>> LifecycleBase
	<<abstract>> SocketWrapperBase
	<<interface>> Runnable
	<<interface>> SocketTask
	<<interface>> HttpProcessor
	<<abstract>> AbstractHttpProcessor
	<<interface>> HttpInputBuffer
	<<interface>> RequestHandler
	<<interface>> ResponseHandler
	<<interface>> HttpExternalRequest
	<<interface>> HttpExternalResponse
	<<interface>> HttpApiHandler
	<<abstract>> AbstractHttpApiHandler

    ServerLifecycle ..|> LifecycleBase
    ServerLifecycle --> WebServiceLifecycle
    WebServiceLifecycle ..|> LifecycleBase
    WebServiceLifecycle --> WebService
    LifecycleBase ..|> Lifecycle
    ServerExecutor --> ServerLifecycle
    ServerLifecycle --> WebServer
    ServerLifecycle --> SocketTaskExecutor
    BioSocketWrapper ..|> SocketWrapperBase
    SocketTaskHandler ..|> SocketTask
    SocketTaskHandler --> SocketWrapperBase
    SocketTask ..|> Runnable
    SocketTaskExecutor --> SocketTaskPool
    SocketTaskExecutor --> ThreadPoolExecutor
    SocketTaskPool --> SocketTaskHandler
    SocketTaskHandler --> HttpProtocolHandler
    HttpProtocolHandler --> HttpProcessorPool
    HttpProcessorPool --> HttpProcessor
    Http11InputBuffer ..|> HttpInputBuffer
    AbstractHttpProcessor ..|> HttpProcessor
    Http11Processor ..|> AbstractHttpProcessor
    Http11Processor --> Http11InputBuffer
    Http11InputBuffer --> Http11Parser
    Http11Processor --> HttpRequestHandler
    Http11Processor --> HttpResponseHandler
    HttpProtocolHandler --> HttpMapper
    HttpMapper --> WebService
    WebService --> HttpApiHandler
    HttpApiHandler <.. HttpExternalRequest
    HttpApiHandler <.. HttpExternalResponse
    HttpApiHandler <|.. AbstractHttpApiHandler
    AbstractHttpApiHandler --> WebResourceProvider
    RequestHandler ..> HttpExternalRequest
    ResponseHandler ..> HttpExternalResponse
    HttpRequestHandler ..|> RequestHandler
    HttpResponseHandler ..|> ResponseHandler
```




