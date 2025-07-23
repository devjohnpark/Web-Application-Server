package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.ThreadPool;
import org.dochi.webserver.config.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public class SocketTaskPool {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskPool.class);
    // 큐가 비었을 때 대기(blocking)가 필요할때 사용
    // take() 호출시 요소 풀에 반환될때까지 blocking
    // poll() 호출시에는 blocking 하지 않고 null 반환
    // thread-safe in multi-threading
    private final ConcurrentLinkedDeque<SocketTask> queue;
    private final Supplier<SocketTask> supplier;

    public SocketTaskPool(ThreadPoolConfig threadPool, Supplier<SocketTask> supplier) {
        this.queue = new ConcurrentLinkedDeque<>();
        this.supplier = supplier;
        this.initPool(threadPool.getMinSpareThreads(), supplier);
        log.info("SocketTaskPool initialized [Total size: {}]", queue.size());
    }

    private void initPool(int poolSize, Supplier<SocketTask> socketTasks) {
        for (int i = 0; i < poolSize; i++) {
            queue.offer(socketTasks.get());
        }
    }

    public void recycle(SocketTask socketTask) {
        queue.addFirst(socketTask); // LIFO
    }

    public SocketTask get() {
        SocketTask socketTask = queue.pollFirst(); // LIFO
        return socketTask != null ? socketTask : supplier.get();
    }

    public int getPoolSize() {
        return queue.size();
    }
}
