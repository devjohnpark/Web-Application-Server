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
        List<ServerLifecycle> allWebServers = validateWebServerList();

        // 웹서버 모두 시작 (부분 실패 시 모두 정지)
        try {
            for (ServerLifecycle s : allWebServers) {
                s.start();
            }
            log.info("All servers started: {}", allWebServers.size());
        } catch (Exception e) {
            log.error("Failed to start servers", e);
            stopAll(allWebServers);
            return;
        }
        awaitMainThread(allWebServers);
    }

    private static List<ServerLifecycle> validateWebServerList() {
        List<ServerLifecycle> allWebServers = new ArrayList<>(servers.values());
        if (allWebServers.isEmpty()) {
            log.error("No web servers found.");
            throw new IllegalStateException("No web servers found.");
        }
        return allWebServers;
    }

    private static void awaitMainThread(List<ServerLifecycle> started) {
        // 메인 스레드 종료를 막음
        CountDownLatch latch = new CountDownLatch(1);

        // 종료 훅 정상 종료, 외부 신호에 의한 종료시 훅으로 stop 후 메인 스레드 대기 중지
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopAll(started);
            latch.countDown();
        }, "shutdown-hook"));

        // 메인 스레드 대기
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stopAll(started);
        }
    }

    private static void stopAll(List<ServerLifecycle> started) {
        for (ServerLifecycle serverLifecycle : started) {
            try {
                serverLifecycle.stop();
            } catch (Exception e) {
                log.error("Stop failed", e);
            }
        }
        log.info("All web servers stopped.");
    }
}
