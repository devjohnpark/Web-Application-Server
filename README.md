# Building a Web Application Server

---
## Overview

병역특례로 호스팅 회사에서 시스템 엔지니어로 근무하며 서버 하드웨어나 소프트웨어를 처음 접하게 되면서 서버 개발에 관심이 생겼습니다. 업무 중 하나는 Apache, Nginx, Tomcat을 설치와 운영을 했었습니다. 그러나 이러한 작업만으로는 동작 원리를 이해하기 어렵다고 느꼈기에 직접 WAS(Web Application Server)를 Java로 구현하고 지속적으로 구조와 성능을 개선해왔습니다. 

* [Sequence & Class Diagram](https://github.com/devjohnpark/Web-Application-Server/wiki/Custom-WAS-%EC%8B%9C%ED%80%80%EC%8A%A4-%26-%ED%81%B4%EB%9E%98%EC%8A%A4-%EB%8B%A4%EC%9D%B4%EC%96%B4%EA%B7%B8%EB%9E%A8)
* [Problem Sloving](https://github.com/devjohnpark/Web-Application-Server/wiki)
	1. HTTP 요청 처리 성능과 HTTP 버전 확장성 개선
	2. TCP 연결과 해제 비용 절감하기 위한 지속 연결 구현
	3. HTTP부터 TCP까지 문제 해결

---
## WAS Architecture

### Architecture Overview

```bash
WebAppServer                 # 서버 설정 진입점 (구성 + start/stop)
 ├── Server                 # 전체 WAS 라이프사이클 루트
 │    ├── ShutdownTasksManager   # JVM 종료 시 실행할 작업 관리
 │    ├── WebService             # Request Path -> HttpApiHandler 라우팅
 │    │    └── HttpApiHandler
 │    │         └── AbstractHttpApiHandler
 │    │              ├── DefaultHttpApiHandler
 │    │              └── CustomHttpApiHandler
 │    └── HttpProtocolHandler    # HTTP 프로토콜 처리 조합 
 │         ├── InternalAdapter   # internal(low-level) <-> external(high-level) 객체 변환
 │         │    ├── connector.Request
 │         │    │    └── RequestFacade
 │         │    │         └── RequestContext
 │         │    └── connector.Response
 │         │         └── ResponseFacade
 │         │              └── ResponseContext
 │         └── AbstractEndpoint  # 네트워크 엔드포인트 (소켓 수락 + 스레드 실행)
 │              └── BioEndpoint
 │                   ├── Acceptor            # 클라이언트 연결 수락 루프
 │                   ├── ThreadPoolExecutor # 요청 처리 워커 스레드 풀
 │                   │    ├── ScalableTaskQueue     # 동적 확장 유도 큐
 │                   │    └── ForceTaskQueuePolicy  # 큐 강제 적재 정책
 │                   └── AbstractSocketTask        # 소켓 단위 작업 실행
 │                        └── BioSocketTask
 │                             └── ConnectionHandler  # 연결 상태 기반 요청 처리 루프
 │                                  └── ProcessorRecycler # HttpProcessor 재사용 풀 (LIFO)
 │                                       └── Http11Processor  # HTTP/1.1 요청 처리 + Keep-Alive 루프
 │                                            ├── internal.Request
 │                                            │    └── RequestContext
 │                                            └── internal.Response
 │                                                 └── ResponseContext
```
### Package Structure

```bash
org.dochi
 ├── api.handler        # 개발자가 구현하는 HTTP API 핸들러 계층
 │    ├── HttpApiHandler
 │    ├── AbstractHttpApiHandler
 │    └── DefaultHttpApiHandler
 ├── connector          # 저수준 internal 요청/응답을 고수준 API 객체로 연결하는 어댑터 계층
 │    ├── Adapter
 │    ├── InternalAdapter
 │    ├── RequestFacade
 │    ├── ResponseFacade
 │    ├── Request
 │    ├── Response
 ├── external           # 개발자에게 노출되는 외부 요청/응답 인터페이스
 │    ├── ExternalRequest
 │    └── ExternalResponse
 ├── internal           # HTTP 파싱, 프로토콜 처리, 재사용 객체 관리 등 저수준 요청 처리 계층
 │    ├── Request
 │    ├── Response
 │    ├── RequestContext
 │    ├── ResponseContext
 │    ├── HttpProcessor
 │    ├── AbstractHttpProcessor
 │    ├── HttpProtocolHandler
 │    ├── ConnectionHandler
 │    ├── ProcessorRecycler
 │    ├── buffer        # 입력/출력 버퍼링 관련 구성요소
 │    │    ├── InputBuffer
 │    │    ├── ApplicationBufferHandler
 │    │    └── TmpBufferedOutputStream
 │    └── http11        # HTTP/1.1 전용 파싱 및 처리 구현체
 │         ├── Http11Processor
 │         ├── Http11InputBuffer
 │         └── Http11Parser
 ├── net                # 소켓 수락, 래핑, 작업 실행 등 네트워크 처리 계층
 │    ├── AbstractEndpoint
 │    ├── AbstractSocketWrapper
 │    ├── AbstractSocketTask
 │    ├── BioEndpoint
 │    ├── BioSocketWrapper
 │    └── Acceptor
 ├── thread             # 스레드풀 확장 및 작업 큐 정책
 │    ├── ScalableTaskQueue
 │    └── ForceTaskQueuePolicy
 ├── webresource        # 정적 리소스 탐색 및 제공 계층
 │    ├── WebResourceProvider
 │    ├── Resource
 │    ├── ResourceType
 └── webserver          # 서버 부트스트랩 및 라이프사이클 관리 계층
      ├── bootstrap
      │    ├── WebAppServer
      │    ├── Server
      │    ├── WebService
      │    ├── ShutdownTasksManager
      │    └── ShutdownTasksProcessor
      └── lifecycle
           ├── Lifecycle
           ├── AbstractLifecycle
           └── LifecycleException
```


---
## 주요 사용 기능

- **HTTP/1.1 지원 (향후 2.0 지원)**: HTTP/1.1 메세지 파싱/직렬화 , Keep-Alive 타임아웃/최대 개수 설정, 요청 처리 파이프라이닝, 
- **BIO 기반 네트워크 I/O (향후 NIO 지원)**: `java.net.Socket`을 사용한 Blocking I/O
- **HTTP API 핸들러**
	- 경로 별 `HttpApiHandler` 등록 (`/` 기본 핸들러 자동 등록, 미매칭 시 루트 핸들러 반환)
	- GET, POST, PUT, PATCH, DELETE 처리 및 미지원 메서드에 대한 에러 응답(405/501)
- **HTML Form 데이터 파싱**
	- `application/x-www-form-urlencoded` 파라메터로 접근 가능
	- `multipart/form-data`: 각 파트별로 접근 가능, 임시 파일 관리
- **정적 리소스 서빙**:  루트 디렉터리 내 파일 탐색 및 읽기, JAR 파일 내부 리소스 탐색(Embedded JAR 지원)
- **다이나믹 워커 스레드풀**: 요청된 소켓 작업 수에 따라 지정한 스레드 풀 최소/최대 크기만큼 동적으로 확장/축소 
- **가상 스레드 지원**: Java 21 이상에서 `useVirtualThreads` 옵션으로 활성화
	- 커널 스레드의 블로킹을 대폭 줄여서 NIO 모델 기반의 WAS 성능 도달 
	* 단, 워커 스레드 내부 로직에 `synchronized` 사용시 성능 저하 발생


---
## 시작하기

###  의존성 추가

현재 Maven/Gradle 중앙 저장소에 배포되어 있지 않으므로, 로컬 빌드 후 의존성으로 추가하거나 소스를 직접 포함하여 사용합니다.

다음은 빌드 시스템에 따른 의존성 설정 예시입니다.
#### 예시1) Gradle

`dochi.jar` 파일을 프로젝트의 특정 경로에 저장합니다.
```bash
project/
 ├─ build.gradle
 ├─ settings.gradle
 ├─ libs/
 │   └─ dochi.jar
 └─ src/
```

`build.gradle`에 `dochi.jar` 파일을 저장한 경로에 대해 의존성을 설정한다.
```groovy
dependencies {  
    implementation files('libs/dochi.jar')  
}
```

#### 예시2) Intellij

1. File
2. Project Structure
3. Libraries
4. Add `dochi.jar`


### WAS 실행

```java
import org.dochi.webserver.bootstrap.WebAppServer;
import org.dochi.webserver.lifecycle.LifecycleException;

public class WebAppServerLauncher {
    public static void main(String[] args) throws LifecycleException {
        WebAppServer server = new WebAppServer(8080);
        server.start();
    }
}
```

기본 설정으로 `8080` 포트에서 서버가 시작되며, `webapp/` 디렉터리의 정적 리소스를 서빙합니다.

### 다중 WAS 인스턴스 동시 실행

각 `WebAppServer` 인스턴스는 포트와 호스트 네임으로 독립적인 서버를 나타냅니다. 
* 포트와 호스트 네임이 모두 같은 경우에 `LifecycleException`이 발생합니다. 
* JVM 종료시 `ShutdownTasksManager`가 실행했던 순서대로 각 서버 인스터스를 안전하게 종료합니다.
* 스레드풀은 인스턴스마다 독립적으로 생성되므로, 인스턴스 수와 최대 스레드 수를 함께 고려하여 시스템 리소스를 설계해야 합니다.

```java
public class WebAppServerLauncher {
    public static void main(String[] args) throws LifecycleException {
        // 8080 포트에 바인딩된 독립 서버 인스턴스
        WebAppServer server1 = new WebAppServer(80, "localhost");
        frontServer.getWebService().setWebResourceRootPath("webapp");

        // 9090 포트에 바인딩된 독립 서버 인스턴스
        WebAppServer server2 = new WebAppServer(9090, "0.0.0.0");
        server2.getWebService()
            .addService("/api/user", new UserApiHandler())
            .addService("/api/post", new PostApiHandler());
        server2.getThreadPool().setMinSpareThreads(200);
        server2.getThreadPool().setMaxThreads(2000);

        // 각 인스턴스 독립적으로 시작 (포트가 달라야 함)
        server1.start();
        server2.start();
    }
}
```

---
### HTTP API 핸들러 작성

`AbstractHttpApiHandler`를 상속하여 원하는 HTTP 메서드를 오버라이드합니다.

```java
import org.dochi.api.handler.AbstractHttpApiHandler;
import org.dochi.external.ExternalRequest;
import org.dochi.external.ExternalResponse;
import org.dochi.http.utils.HttpStatus;

import java.io.IOException;

public class UserApiHandler extends AbstractHttpApiHandler {

    @Override
    protected void doGet(ExternalRequest request, ExternalResponse response) throws IOException {
        String userId = request.getParameter("userId");
        // ...
        response.send("userId=" + userId, "text/plain; charset=utf-8");
    }

    @Override
    protected void doPost(ExternalRequest request, ExternalResponse response) throws IOException {
        // Content-Type: application/x-www-form-urlencoded 자동 파싱
        String name     = request.getParameter("name");
        String email    = request.getParameter("email");
        // ...
        response.setStatus(HttpStatus.CREATED).send();
    }
}
```

### 핸들러 등록

```java
WebAppServer server = new WebAppServer(8080);

server.getWebService()
    .addService("/api/user", new UserApiHandler())
    .addService("/api/post", new PostApiHandler());

server.start();
```

- `/` 경로에는 기본적으로 `DefaultHttpApiHandler`(정적 리소스 서빙)가 등록되어 있습니다.
- 경로가 일치하는 핸들러가 없으면 `/` 핸들러로 폴백됩니다.

---
### 서버 설정

`WebAppServer` 객체를 통해 포트, 스레드풀, 소켓, HTTP 제한값 등을 설정할 수 있습니다.

```java
WebAppServer server = new WebAppServer(8080, "localhost");

// 워커 스레드풀 설정
server.getThreadPool().setMinSpareThreads(100);
server.getThreadPool().setMaxThreads(1000);
server.getThreadPool().setUseVirtualThreads(false); // Java 21+에서 true로 설정 가능

// 소켓 설정
server.getSocket().setKeepAliveTimeout(5000);       // ms 단위
server.getSocket().setMaxKeepAliveRequests(100);

// HTTP 요청 메세지 크기 제한
server.getHttp().getReqConfig().setRequestHeaderMaxSize(16 * 1024);   // 16KB
server.getHttp().getReqConfig().setRequestPayloadMaxSize(10 * 1024 * 1024); // 10MB

// HTTP 응답 메세지 크기 제한
server.getHttp().getResConfig().setResponseHeaderMaxSize(8 * 1024);   // 8KB
server.getHttp().getResConfig().setResponseBodyMaxSize(4 * 1024 * 1024); // 4MB

// 정적 리소스 루트 디렉터리 변경 (기본값: "webapp")
server.getWebService().setWebResourceRootPath("static");

server.start();
```

### 설정 항목 기본값 요약

| 항목                 | 기본값         |
| ------------------ | ----------- |
| 포트                 | `8080`      |
| 호스트                | `localhost` |
| 최소 스레드 수           | `500`       |
| 최대 스레드 수           | `3000`      |
| 가상 스레드 사용          | `false`     |
| Keep-Alive 타임아웃    | `5000ms`    |
| 최대 Keep-Alive 요청 수 | `50`        |
| 요청 헤더 최대 크기        | `8KB`       |
| 요청 바디 최대 크기        | `2MB`       |
| 응답 헤더 최대 크기        | `8KB`       |
| 응답 바디 최대 크기        | `2MB`       |
| 정적 리소스 루트 디렉터리     | `webapp`    |

---
### 요청 처리

`ExternalRequest` 인터페이스를 통해 HTTP 요청 데이터에 접근합니다.

```java
// 기본 메타데이터
String method      = request.getMethod();         // "GET", "POST", ...
String uri         = request.getRequestURI();     // "/api/user?id=1"
String path        = request.getPath();           // "/api/user"
String query       = request.getQueryString();    // "id=1"
String protocol    = request.getProtocol();       // "HTTP/1.1"
String contentType = request.getContentType();    // "application/json"
int contentLength  = request.getContentLength();

// 헤더 조회
String host        = request.getHeader("Host");
String auth        = request.getHeader("Authorization");

// 파라미터 조회 (쿼리스트링 + application/x-www-form-urlencoded 자동 파싱)
String userId      = request.getParameter("userId");

// 입력 스트림 직접 접근
InputStream in     = request.getInputStream();
```


### 응답 처리

`ExternalResponse` 인터페이스를 통해 HTTP 응답을 구성합니다.  
메서드 체이닝을 지원하며, `send()` 또는 `sendError()` 호출 시점에 응답이 커밋됩니다.

```java
// 텍스트 응답
response.send("Hello, World!", "text/plain; charset=utf-8");

// 상태코드와 헤더를 직접 설정
response
    .setStatus(HttpStatus.CREATED)
    .setHeader("X-Custom-Header", "value")
    .setCookie("sessionId=abc123; HttpOnly")
    .send(responseBody, "application/json");

// 빈 응답 (헤더만 전송)
response.setStatus(HttpStatus.NO_CONTENT).send();

// 에러 응답
response.sendError(HttpStatus.NOT_FOUND);
response.sendError(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");

// 스트리밍 응답 (OutputStream 직접 사용)
OutputStream out = response.getOutputStream();
out.write(data);
```

---

### 정적 리소스 서빙

기본적으로 프로젝트 루트의 `webapp/` 디렉터리에서 정적 파일을 서빙합니다.

```bash
project-root/
└── webapp/
    ├── index.html    -> GET / 요청 시 반환
    ├── style.css
    └── images/
        └── logo.png
```

- `/` 경로 요청 시 자동으로 `index.html`을 반환합니다.
- 실행 가능한 JAR로 패키징된 경우, JAR 내부의 리소스도 자동으로 탐색합니다.
- 지원하는 MIME 타입: `text/html`, `text/css`, `text/plain`, `application/javascript`, `application/json`, `application/xml`, `image/png`, `image/jpeg`, `image/x-icon` 등

---
### 멀티파트(Multipart) 처리

`multipart/form-data` 요청에서 `getPart()` 메서드로 각 파트(필드 또는 파일)에 접근합니다.

```java
@Override
protected void doPost(ExternalRequest request, ExternalResponse response) throws IOException {
    // 일반 텍스트 필드
    Part namePart = request.getPart("name");
    String name = new String(namePart.getContent());

    // 파일 업로드
    Part filePart = request.getPart("profile");
    if (filePart.isFile()) {
        String fileName    = filePart.getFileName();
        String contentType = filePart.getContentType();
        byte[] fileData    = filePart.getContent();
        // 파일 처리 ...
    }

    response.setStatus(HttpStatus.CREATED).send();
}
```

업로드된 파일은 내부적으로 `multipart-tmp-file/` 디렉터리에 파일 이름 증복을 막기 위해 UUID 기반으로 임시 저장되며, 요청 처리가 완료된 후 자동으로 삭제됩니다.

---
### 라이선스

이 프로젝트는 학습 및 실험 목적으로 제작되었습니다.
