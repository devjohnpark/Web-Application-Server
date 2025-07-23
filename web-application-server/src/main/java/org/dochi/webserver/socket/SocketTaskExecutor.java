package org.dochi.webserver.socket;

import org.dochi.webserver.config.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketTaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskExecutor.class);

    private final SocketTaskPool taskPool;
    private final ThreadPoolExecutor workerThreadPoolExecutor;

    public SocketTaskExecutor(ThreadPoolConfig threadPool, SocketTaskPool taskPool) {
        this.workerThreadPoolExecutor = new ThreadPoolExecutor(
                threadPool.getMinSpareThreads(),
                threadPool.getMaxThreads(),
                60L, // corePoolSize을 초과하는 스레드가 할당된 작업이 없는 경우 keepAliveTime이 경과한 뒤 제거
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>() // 작업(Task) 대기 큐, 스레드 풀이 모두 바쁠 경우에 추가로 들어오는 작업(SocketTaskHandler)을 일시적으로 저장
        );

        // Core Pool 개수 만큼 스레드를 미리 생성하여 성능 최적화
        workerThreadPoolExecutor.prestartAllCoreThreads();

        log.info("Worker Thread Pool Executor initialized [Total size: {}]", workerThreadPoolExecutor.getPoolSize());

        registerShutdownHook();
        this.taskPool = taskPool;
    }

    public void execute(SocketWrapperBase<?> socketWrapper) {
        try {
            // 사용 가능한 SocketTask 가져오기
            SocketTask socketTask = taskPool.get();

            // 새로 연결된 소켓 설정
            socketTask.setSocketWrapper(socketWrapper);

            workerThreadPoolExecutor.execute(() -> {
                try {
                    socketTask.run();
                } finally {
                    taskPool.recycle(socketTask); // socketTask 실행 완료 후 객체 풀에 반환
                }
            });
        } catch (Exception e) {
            log.error("Error executing socket task: {}", e.getMessage(), e);
            throw new RuntimeException("Execution failed", e);
        }
    }

    private void registerShutdownHook() {
        // JVM이 종료될 때 ShutdownHook 스레드 실행
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownGracefully));
    }

    // 스레드 풀에 남아 있는 대기 중인 작업을 모두 취소
    // 1. 진행 중인 작업이 완료될 때까지 일정 시간 동안 기다림
    // 2. 일정 시간 최과시 스레드 풀이 강제 종료
    private void shutdownGracefully() {
        log.info("Worker thread pool shutdown has started.");
        try {
            // 새로운 작업 수락 중지
            workerThreadPoolExecutor.shutdown();

            // 진행 중인 모든 작업이 완료될 때까지 대기 (true를 반환하면 모든 작업이 종료됨었음을 의미)
            if (!workerThreadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                // 최대 60초 후에도 종료되지 않은 경우 강제 종료
                workerThreadPoolExecutor.shutdownNow();
            }
        } catch (InterruptedException e) { // 스레드가 대기 중(sleep(), wait(), join() 메서드가 호출된 상태) 인터럽트 신호를 받으면 예외가 발생
            // 인터럽트 발생 시 강제 종료 (더 이상 대기하거나 작업을 계속할 필요가 없다고 판단)
            workerThreadPoolExecutor.shutdownNow();

            // 현재 메서드가 인터럽트를 받았다는 신호를 상위 호출자에게 전달하려면 상태를 복구
            // 현재 스레드의 인터럽트 상태를 true로 설정하고, 상위 호출자는 Thread.currentThread().isInterrupted()로 인터럽트 상태 확인 가능
            Thread.currentThread().interrupt();
            log.error("Worker thread pool shutdown has interrupted.", e);
        } finally {
            log.info("Worker thread pool shutdown has completed.");
        }
    }
}
