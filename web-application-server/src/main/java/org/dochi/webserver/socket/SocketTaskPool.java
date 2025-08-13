package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public class SocketTaskPool {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskPool.class);
    // 큐가 비었을 때 대기(blocking)가 필요할때 사용
    // take() 호출시 요소 풀에 반환될때까지 blocking)
    // poll() 호출시에는 blocking 하지 않고 null 반환
    // thread-safe in multi-threading
    private final LinkedBlockingQueue<SocketTask> queue;
    private final Supplier<SocketTask> supplier;

    public SocketTaskPool(ThreadPool threadPool, Supplier<SocketTask> supplier) {
        this.queue = new LinkedBlockingQueue<>();
        this.supplier = supplier;
        this.initPool(threadPool.getMinSpareThreads(), supplier);
        log.info("SocketTaskPool initialized [Total size: {}]", queue.size());
    }

    private void initPool(int poolSize, Supplier<SocketTask> socketTasks) {
        for (int i = 0; i < poolSize; i++) {
            queue.offer(socketTasks.get()); // 공급자를 통해 객체 생성 및 큐에 추가
        }
    }

    public SocketTask get() {
        SocketTask socketTask = queue.poll();
//        if (socketTask == null || socketTask.getSocketWrapper().isUsing()) {
//            socketTask = supplier.get(); // SocketTask 구현체 생성
//        }
        if (socketTask == null) {
            socketTask = supplier.get(); // SocketTask 구현체 생성
        }
        return socketTask;
    }

//    public SocketTask get() throws InterruptedException {
////        SocketTask socketTask = queue.poll();
////        if (socketTask == null || socketTask.getSocketWrapper().isUsing()) {
////            socketTask = supplier.get(); // SocketTask 구현체 생성
////        }
//        return queue.take();
//    }

//    public SocketTaskHandler getAvailableRequestHandler(KeepAlive keepAlive, HttpApiMapper requestMapper) {
//        SocketTaskHandler requestHandler = requestHandlerQueue.poll();
//        if (requestHandler == null || requestHandler.getSocketWrapper().isUsing()) {
//            requestHandler = new SocketTaskHandler(new BioSocketWrapper(keepAlive), requestMapper);
//        }
//        return requestHandler;
//    }

    public void recycle(SocketTask socketTask) {
        queue.offer(socketTask);
    }

    public int getPoolSize() {
        return queue.size();
    }
}
