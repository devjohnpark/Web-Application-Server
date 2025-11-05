package org.dochi.webserver.connect;

import org.dochi.webserver.attribute.SocketAttribute;
import org.dochi.webserver.net.BioSocketWrapper;
import org.dochi.webserver.net.SocketWrapperBase;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connector {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);

    private volatile SocketWrapperBase<?> serverConnectedSocket;
    private SocketWrapperBase<?> clientConnectedSocket;

    private Thread serverThread;
    private ServerSocket serverSocket;

    public SocketWrapperBase<?> getServerConnectedSocket() {
        return serverConnectedSocket;
    }

    public SocketWrapperBase<?> getClientConnectedSocket() {
        return clientConnectedSocket;
    }

    @BeforeEach
    public void connect() throws IOException {

        serverSocket = new ServerSocket(0); // 사용 가능한 포트 자동 할당

        serverThread = new Thread(() -> {
            log.debug("Start accept thread...");
            if (!serverSocket.isClosed()) {
                try {
                    serverConnectedSocket = new BioSocketWrapper(serverSocket.accept(), new SocketAttribute());
                    serverConnectedSocket.setConnectionTimeout(1000);
                    log.debug("created server's connection socket");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    log.debug("End accept thread");
                }
            }
        });

        serverThread.start();

        clientConnectedSocket = new BioSocketWrapper(new Socket("localhost", serverSocket.getLocalPort()), new SocketAttribute());

        log.debug("Created client's connection socket");

        // 각 스레드는 자신만의 CPU 캐시를 사용할 수 있으며, 변수 값을 메인 메모리와 동기화하지 않고 캐시에 저장할수 있다.따라서 한 스레드에서 변경한 값이 다른 스레드에게 보이지 않는 현상 발생
        // JVM이 빈 while 루프를 최적화하여 serverConnectedSocket 값을 캐시에서만 읽을 수 있음
        // 다른 스레드(serverThread)에서 변경한 값이 main 스레드에서 반영되지 않아서 무한 루프를 돌았다.
        // volatile 사용해서 serverConnectedSocket 선언: 값을 쓸 때도 항상 메인 메모리에 즉시 반영 -> 값을 읽을 때 항상 메인 메모리(main memory) 읽음
        while (serverConnectedSocket == null) {
            Thread.onSpinWait(); // 무한 루프 -> CPU 자원을 계속 낭비 -> 과도한 CPU 사용 방지 -> 루프가 busy-wait임을 알린다.
            // 1. CPU에게 힌트를 줌: 현재 스레드는 짧은 시간 안에 다시 실행될 것이며, 다른 스레드의 작업 결과를 기다리고 있다는 것을 알린다. 그럼 CPU는 전력 소비를 줄이고, 발열을 낮출 수 있음.
            // 2. JIT 컴파일러 최적화 유도: JIT 컴파일러는 whileg 루프를 busy-wait로 인식하고, 불필요한 루프 제거를 막고 적절한 기계어 인스트럭션(YIELD 등)을 삽입한다. 따라서 같은 코어에 있는 다른 스레드에게 다른 스레드에게 CPU를 더 잘 양보함으로써 전체 시스템 성능 향상.
        }

        log.debug("Client and Server connection ready.");
    }

    @AfterEach
    public void disconnect() throws IOException {
        if (clientConnectedSocket != null && !clientConnectedSocket.isClosed()) {
            clientConnectedSocket.close();
            log.debug("Client socket close");
        }

        if (serverConnectedSocket != null && !serverConnectedSocket.isClosed()) {
            serverConnectedSocket.close();
            log.debug("Server socket wrapper close");
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            log.debug("Server socket close");
        }

        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            log.debug("server thread interrupted");
        }
    }


}