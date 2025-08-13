package org.dochi.webserver.socket;

import org.dochi.webserver.executor.WorkerPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class SocketTaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskExecutor.class);

    private final WorkerPoolExecutor workerExecutor;
    private final SocketTaskPool taskPool;

    public SocketTaskExecutor(WorkerPoolExecutor workerExecutor, SocketTaskPool taskPool) {
        this.workerExecutor = workerExecutor;
        this.taskPool = taskPool;
    }

//    public void execute(Socket connectedSocket) {
//        // 사용가능한 SocketTask 구현체 가져오기
//        SocketTask socketTask = taskPool.get();
//
//        // 새롭게 연결된 소켓 설정
//        socketTask.getSocketWrapper().setConnectedSocket(connectedSocket);
//
//        // WorkerPoolExecutor 객체로 SocketTask 구현체를 ThreadPoolExecutor 객체에 제출해서 비동기 실행
//        // SocketTask 객체 ThreadPoolExecutor 객체에 제출 후 즉각 SocketTask 구현체 반환
//        // 반환된 SocketTask 구현체 RequestTaskPool의 큐에 푸시
//        taskPool.recycle(workerExecutor.execute(socketTask));
//    }

    public void execute(Socket connectedSocket) {
        CompletableFuture.runAsync(() -> {
            try {
                // 사용 가능한 SocketTask 가져오기
                SocketTask socketTask = taskPool.get();

                // 새로 연결된 소켓 설정
                socketTask.getSocketWrapper().setConnectedSocket(connectedSocket);

                // SocketTask 구현체의 실행이 모두 마친후에 객체풀에 반환하기 위해서 WorkerExecutor.execute 메서드를 synchronous로 처리 (단, WorkerExecutor 내부의 ThreadPoolExecutor로 비동기 실행)
                // 그리하여 SocketTaskExecutor.execute() 내부 로직을 비동기로 실행시켜서 blocking 되지 않도록 했다.
                // 안그러면 실행되지 않은 SocketTask 구현체가 반환된다.
                taskPool.recycle(workerExecutor.execute(socketTask));
            } catch (Exception e) {
                log.error("Error executing socket task: {}", e.getMessage(), e);
                throw new RuntimeException("Execution failed", e);
            }
        });
    }
}
