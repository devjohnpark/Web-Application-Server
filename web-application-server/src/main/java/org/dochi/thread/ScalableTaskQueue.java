package org.dochi.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

// 문제 상황
// 1. WAS의 소켓 작업을 실행하기에 적합한 Executor 구현체 필요
// 2. Executor 구현체중 스레드풀을 사용하고 스레드 개수를 증감할수있는 ThreadPoolExecutor 선택
// 3. ThreadPoolExecutor에 필요한 작업 큐는 BlockingQueue 구현체
// 4. WAS는 Thread per Connection 모델이므로 큐에서 개수 제한없이 소켓 작업들이 대기 할수있는 구조 필요
// 5. LinkedBlockingQueue은 작업이 무제한 적재가능해서 선택
// 6. ThreadPoolExecutor에 정책에 의해 작업이 큐에 무제한 적재되면서 maximumPoolSize까지 스레드풀을 확장시킬수 없게 구현됨
// 7. 소켓 작업이 큐에 무제한 적재되면서 지정한 개수까지 스레드풀의 확장/축소 필요

// 결론: WAS의 소켓 작업이 무제한 적재되는 큐 + maximumPoolSize까지 ThreadPoolExecutor의 스레드풀 확장을 지원하는 커스텀 LinkedBlockingQueue
// 문제: LinkedBlockingQueue()의 크기를 Integer.MAX_VALUE 용량으로 ThreadPoolExecutor에 주입했을 경우, ThreadPoolExecutor에 정책에의해 작업이 큐에 무제한 적재되면서 maximumPoolSize까지 스레드풀을 확장시킬수없다.
// 원인: 1. 워커 스레드 수가 corePoolSize 이상일 경우, 작업 큐에 적재(offer) 성공(true)시 그대로 진행
//      2. offer시 적재된 작업이 LinkedBlockingQueue의 크기를 초과하면 false 반환
// 접근
// 1: ThreadPoolExecutor 상속해서 커스텀: ThreadPoolExecutor의 워커 스레드 추가 메서드인 addWorker()가 private 접근 지정자로 정의되어있어 자식 클래스도 호출 불가
// 2: 1방법이 안되므로 LinkedBlockingQueue에서 유휴 스레드가 존재하지 않으면 false 반환하여 ThreadPoolExecutor가 새 스레드 생성하도록 트릭 (maximumPoolSize 까지)
// 해결책
// 1. 유휴 스레드가 존재하면 작업 큐에 즉각 적재
// 2. 유휴 스레드가 존재하지 않으면 false 반환하여 ThreadPoolExecutor가 새 스레드 생성하도록 트릭
// 3. 스레드풀 크기가 max 도달하면 큐에 계속 적재 (unlimit 이므로 항상 성공)
public class ScalableTaskQueue extends LinkedBlockingQueue<Runnable> {
    private static final Logger log = LoggerFactory.getLogger(ScalableTaskQueue.class);

    private volatile ThreadPoolExecutor executor;

    public ScalableTaskQueue() {
        super(Integer.MAX_VALUE); // unlimit
    }

    // 큐가 executor의 상태를 조회해야 하므로 생성 후 주입
    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public boolean offer(Runnable task) {
        if (executor == null) {
            return super.offer(task);
        }

        int poolSize = executor.getPoolSize(); // 워커 스레드 풀 크기
        int activeCount = executor.getActiveCount(); // 현재 활성화된 스레드 개수
        int maxPoolSize = executor.getMaximumPoolSize(); // 최대 스레드 개수

        // 유휴 스레드의 개수: poolSize - activeCount

        // 유휴 스레드가 존재하면 작업 큐에 즉각 적재
        if (activeCount < poolSize) {
            return super.offer(task);
        }

        // 유휴 스레드가 존재하지 않으면 false 반환하여 ThreadPoolExecutor가 새 스레드 생성하도록 트릭
        if (poolSize < maxPoolSize) {
            return false;
        }

        // 스레드풀 크기가 max 도달하면 큐에 계속 적재 (큐의 크기가 Integer.MAX_VALUE 이므로 항상 성공)
        return super.offer(task);
    }

    // ForceQueuePolicy에서 reject()된 작업을 강제로 큐에 넣을 때 사용
    public boolean forceOffer(Runnable task) {
        return super.offer(task);
    }
}