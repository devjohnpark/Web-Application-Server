package org.dochi.webserver.executor;

import org.dochi.webserver.attribute.WebServer;
import org.dochi.webserver.lifecycle.LifecycleException;
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
    private static final Map<Integer, ServerLifecycle> servers = new HashMap<>();

    private ServerExecutor() {}

    public static void addWebServer(WebServer webServer) {
        if (servers.containsKey(webServer.getPort())) {
            log.error("Web server already exists: {}", webServer.getPort());
            throw new IllegalArgumentException("Web server has already exists.");
        }
        servers.put(webServer.getPort(), new ServerLifecycle(webServer));
        // 단일 서버 실행/종료를 위한 cli 대기 스레드 생성 후 put
    }

//    public static void execute() {
//        List<ServerLifecycle> allWebServers = new ArrayList<>(servers.values());
//        if (allWebServers.isEmpty()) {
//            log.error("No web servers found.");
//            throw new IllegalStateException("No web servers found.");
//        }
//        try(ExecutorService executor = Executors.newFixedThreadPool(allWebServers.size())) {
//            for (ServerLifecycle serverLifecycle : allWebServers) {
//                executor.submit(serverLifecycle::start);
//            }
//            registerShutdownHook(allWebServers);
//        }
//    }

    public static void execute() {
        List<ServerLifecycle> allWebServers = new ArrayList<>(servers.values());
        if (allWebServers.isEmpty()) {
            log.error("No web servers found.");
            throw new IllegalStateException("No web servers found.");
        }

        // ExecutorService 인터페이스 내부 close 메서드에서 shutdown()을 호출하고 terminated = awaitTermination(1L, TimeUnit.DAYS) 를 호출해서 하루 뒤에 ExecutorService 스레드 종료된다.
        // ThreadPoolExecutor의 awaitTermination 메서드 내에서는 타임아웃(nanos <= 0L)이 발생하면 루프가 반복되지 않고 false를 반환하며 종료된다.
        // 그러나 ExecutorService 인터페이스의 close() 메서드의 while (!terminated) 루프는 awaitTermination이 false를 반환해도 스레드 풀이 TERMINATED가 될 때까지 계속 반복(while(runStateLessThan())한다.
        // 따라서 ExecutorService의 close() 전체를 고려한다면: 타임아웃되어도 TERMINATED가 되지 않으면 상위 루프가 반복되어 close() 되지 못하는 구조이다.
        try(ExecutorService executor = Executors.newFixedThreadPool(allWebServers.size())) {
            for (ServerLifecycle serverLifecycle : allWebServers) {
//                executor.submit(serverLifecycle::start);
                executor.submit(() -> {
                    try {
                        serverLifecycle.start();
                    } catch (Exception e) {
                        // 서버 인스턴스 하나라도 예외 발생하면 ExecutorService 정료됨 (모든 서버 인스턴스 종료)
                        // ThreadPoolExecutor의 내부 클래스 Worker.runWorker()에서 예외 발생시, processWorkerExit()에서 tryTerminate() 호출하여 ExecutorService 종료
                        log.error("Server exit - ServerLifecycle occur exception: ", e);
                    }
                });
            }
            registerShutdownHook(allWebServers, executor);
        }
    }

    private static void registerShutdownHook(List<ServerLifecycle> allWebServers, ExecutorService executor) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopServers(allWebServers, executor);
        }));
    }

    private static void stopServers(List<ServerLifecycle> allWebServers, ExecutorService executor) {
        for (ServerLifecycle serverLifecycle : allWebServers) {
            try {
                serverLifecycle.stop();
            } catch (Exception e) {
                log.error("ServerLifecycle occur exception: ", e);
            }
        }
        executor.shutdown();
    }
}
