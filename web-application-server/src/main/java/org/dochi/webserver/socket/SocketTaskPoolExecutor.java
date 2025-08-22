package org.dochi.webserver.socket;
//
//import org.dochi.webserver.attribute.ThreadPool;
//import org.dochi.webserver.executor.WorkerPoolExecutor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.net.Socket;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//public class SocketTaskExecutor {
//    private static final Logger log = LoggerFactory.getLogger(SocketTaskExecutor.class);
//
//    private final WorkerPoolExecutor workerExecutor;
//    private final SocketTaskPool taskPool;
//
//    public SocketTaskExecutor(WorkerPoolExecutor workerExecutor, SocketTaskPool taskPool) {
//        this.workerExecutor = workerExecutor;
//        this.taskPool = taskPool;
//    }
//
////    public void execute(Socket connectedSocket) {
////        // 사용가능한 SocketTask 구현체 가져오기
////        SocketTask socketTask = taskPool.get();
////
////        // 새롭게 연결된 소켓 설정
////        socketTask.getSocketWrapper().setConnectedSocket(connectedSocket);
////
////        // WorkerPoolExecutor 객체로 SocketTask 구현체를 ThreadPoolExecutor 객체에 제출해서 비동기 실행
////        // SocketTask 객체 ThreadPoolExecutor 객체에 제출 후 즉각 SocketTask 구현체 반환
////        // 반환된 SocketTask 구현체 RequestTaskPool의 큐에 푸시
////        taskPool.recycle(workerExecutor.execute(socketTask));
////    }
//
//    public void execute(Socket connectedSocket) {
//        CompletableFuture.runAsync(() -> {
//            try {
//                // 사용 가능한 SocketTask 가져오기
//                SocketTask socketTask = taskPool.get();
//
//                // 새로 연결된 소켓 설정
//                socketTask.getSocketWrapper().setConnectedSocket(connectedSocket);
//
//                // SocketTask 구현체의 실행이 모두 마친후에 객체풀에 반환하기 위해서 WorkerExecutor.execute 메서드를 synchronous로 처리 (단, WorkerExecutor 내부의 ThreadPoolExecutor로 비동기 실행)
//                // 그리하여 SocketTaskExecutor.execute() 내부 로직을 비동기로 실행시켜서 blocking 되지 않도록 했다.
//                // 안그러면 실행되지 않은 SocketTask 구현체가 반환된다.
//                taskPool.recycle(workerExecutor.execute(socketTask));
//            } catch (Exception e) {
//                log.error("Error executing socket task: {}", e.getMessage(), e);
//                throw new RuntimeException("Execution failed", e);
//            }
//        });
//    }
//}



//package org.dochi.webserver.executor;

import org.dochi.webserver.attribute.ThreadPool;
import org.dochi.webserver.socket.SocketTask;
import org.dochi.webserver.socket.SocketTaskPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketTaskPoolExecutor {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskPoolExecutor.class);
    private final SocketTaskPool taskPool;
    private final ThreadPoolExecutor workerThreadPoolExecutor;

    public SocketTaskPoolExecutor(ThreadPool threadPool, SocketTaskPool taskPool) {
        this.workerThreadPoolExecutor = new ThreadPoolExecutor(
                threadPool.getMinSpareThreads(),
                threadPool.getMaxThreads(),
                60L, // corePoolSize을 초과하는 스레드가 할당된 작업이 없는 경우 keepAliveTime이 경과한 뒤 제거
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );

        log.info("Worker Thread Pool Executor initialized [Total size: {}]", workerThreadPoolExecutor.getPoolSize());

        this.taskPool = taskPool;

        this.workerThreadPoolExecutor.prestartAllCoreThreads();
    }

    public void execute(Socket connectedSocket) {
        try {
            workerThreadPoolExecutor.execute(() -> {
                // 사용 가능한 SocketTask 가져오기
                SocketTask socketTask = taskPool.get();

                // 새로 연결된 소켓 설정
                socketTask.getSocketWrapper().setConnectedSocket(connectedSocket);

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

    // 스레드 풀에 남아 있는 대기 중인 작업을 모두 취소
    // 1. 진행 중인 작업이 완료될 때까지 일정 시간 동안 기다림
    // 2. 일정 시간 최과시 스레드 풀이 강제 종료
    public void shutdownGracefully() {
        try {
            // 새로운 작업 수락 중지
            workerThreadPoolExecutor.shutdown();

            // 진행 중인 모든 작업이 완료될 때까지 대기 (true를 반환하면 모든 작업이 종료됨었음을 의미)
            if (!workerThreadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                // 최대 60초 후에도 종료되지 않은 경우 강제 종료
                log.warn("Forcing worker pool shutdownNow()");
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
            log.info("Worker thread pool shutdown completed. [poolSize={}, active={}, queued={}]",
                    workerThreadPoolExecutor.getPoolSize(),
                    workerThreadPoolExecutor.getActiveCount(),
                    workerThreadPoolExecutor.getQueue().size());
        }
    }
}