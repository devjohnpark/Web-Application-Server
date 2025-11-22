package org.dochi.thread;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

// ThreadPoolExecutor가 maximumPoolSize 도달 이후 addWorker 실패로 reject() 된 작업을 큐에 강제 적재하는 핸들러
public class ForceTaskQueuePolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("Executor is already shut down. task=" + task);
        }

        if (!(executor.getQueue() instanceof ScalableTaskQueue queue)) {
            throw new RejectedExecutionException("ForceTaskQueuePolicy requires ScalableTaskQueue. task=" + task);
        }

        // 큐에 강제 적재 (무제한이므로 항상 성공)
        boolean offered = queue.forceOffer(task);
        if (!offered) { throw new RejectedExecutionException("Failed to force offer task to queue. task=" + task); }
    }
}