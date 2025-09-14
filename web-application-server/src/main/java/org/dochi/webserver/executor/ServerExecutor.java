package org.dochi.webserver.executor;

import org.dochi.webserver.attribute.WebServer;
import org.dochi.webserver.lifecycle.ServerLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ServerExecutor {
    private static final Logger log = LoggerFactory.getLogger(ServerExecutor.class);
    private static final Map<WebServer, ServerLifecycle> servers = new HashMap<>();

    private ServerExecutor() {}

    public static void addWebServer(WebServer webServer) {
        if (servers.containsKey(webServer)) {
            log.error("Web server already exists: {}", webServer);
            throw new IllegalArgumentException("Web server already exists: " + webServer);
        }
        servers.put(webServer, new ServerLifecycle(webServer));
        // 단일 서버 실행/종료를 위한 cli 대기 스레드 생성 후 put
    }

    public static void execute() {
        List<ServerLifecycle> allWebServers = new ArrayList<>(servers.values());
        if (allWebServers.isEmpty()) {
            log.error("No web servers found.");
            throw new IllegalStateException("No web servers found.");
        }

        // 1) 모두 시작 (부분 실패 시 이미 시작된 서버는 롤백 정지)
        List<ServerLifecycle> started = new ArrayList<>();
        try {
            for (ServerLifecycle s : allWebServers) {
                s.start();                 // 비차단이어야 함: 내부에서 accept 스레드 시작
                started.add(s);
            }
        } catch (Exception e) {
            log.error("Failed to start some server(s). Rolling back...", e);
            stopAllReverse(started);
        }

        // 2) 종료 훅: 역순으로 안전 종료
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopAllReverse(started);
            latch.countDown();
        }, "shutdown-hook"));

        log.info("All servers started: {}", started.size());

        // 3) 메인 스레드 대기 (신호 오면 훅에서 stop 후 countDown)
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stopAllReverse(started);
        }
        log.info("All web servers stopped.");
    }

    private static void stopAllReverse(List<ServerLifecycle> started) {
        for (int i = started.size() - 1; i >= 0; i--) {
            try { started.get(i).stop(); }
            catch (Exception e) { log.error("Stop failed", e); }
        }
    }

}
