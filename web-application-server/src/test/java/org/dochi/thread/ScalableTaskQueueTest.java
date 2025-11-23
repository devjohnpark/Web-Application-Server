package org.dochi.thread;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ScalableTaskQueueTest {

    private ScalableTaskQueue testQueue;
    private final Runnable dummyTask = () -> {};
    private ThreadPoolExecutor executor;
    private CountDownLatch blockLatch;

    // executor의 activeCount를 원하는 수만큼 고정하기 위해 blockLatch가 열릴 때까지 스레드를 점유
    private Runnable blockingTask() {
        return () -> {
            try {
                blockLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    private static void waitForActiveCount(ThreadPoolExecutor executor, int expected) {
        long deadline = System.currentTimeMillis() + 5000;
        while (executor.getActiveCount() != expected) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError();
            }
            Thread.onSpinWait();
        }
    }

    @BeforeEach
    void setUp() {
        testQueue = new ScalableTaskQueue();
        blockLatch = new CountDownLatch(1);
    }

    @AfterEach
    void tearDown() {
        // blocking task 들이 종료될 수 있도록 latch 해제 후 shutdown
        blockLatch.countDown();
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void offer_whenIdleThreadExists_shouldEnqueueImmediately() {
        executor = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        executor.prestartCoreThread();

        executor.execute(blockingTask());
        executor.execute(blockingTask());
        waitForActiveCount(executor, 2);

        testQueue.setExecutor(executor);

        // when
        boolean result = testQueue.offer(dummyTask);

        // then
        assertThat(result).isTrue();
        assertThat(testQueue.size()).isEqualTo(1);
    }

    @Test
    void offer_whenNoIdleThreadAndPoolNotMax_shouldReturnFalseToTriggerNewThread() {
        executor = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        executor.prestartAllCoreThreads();

        for (int i = 0; i < 4; i++) {
            executor.execute(blockingTask());
        }
        waitForActiveCount(executor,4);

        testQueue.setExecutor(executor);

        // when
        boolean result = testQueue.offer(dummyTask);

        // then
        assertThat(result).isFalse(); // 큐에 적재 안하고 false 반환
        assertThat(testQueue.size()).isEqualTo(0); // 큐에 적재되지 않아야 함
    }

    @Test
    @DisplayName("유휴 스레드가 없고 poolSize == maxPoolSize이면 큐에 적재하고 true를 반환한다")
    void offer_whenNoIdleThreadAndPoolReachedMax_shouldEnqueue() throws InterruptedException {
        // given
        // core=max=4로 설정하여 poolSize와 maxPoolSize가 동일한 상황 구성
        // activeCount=4 → 유휴=0, poolSize(4) == maxPoolSize(4)
        executor = new ThreadPoolExecutor(4, 4, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        executor.prestartAllCoreThreads();

        for (int i = 0; i < 4; i++) {
            executor.execute(blockingTask());
        }
        waitForActiveCount(executor, 4);

        testQueue.setExecutor(executor);

        // when
        boolean result = testQueue.offer(dummyTask);

        // then
        assertThat(result).isTrue();
        assertThat(testQueue.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("작업 제출 이후, 유휴 스레드가 추가로 생성되고 큐에 적재하고 true를 반환한다")
    void offer_whenPoolIsNotEmptyAfterExecute_shouldReturnTrue() {
        // given
        // core=max=4로 설정하여 poolSize와 maxPoolSize가 동일한 상황 구성
        // activeCount=4 → 유휴=0, poolSize(4) == maxPoolSize(4)
        executor = new ThreadPoolExecutor(4, 4, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        executor.prestartAllCoreThreads();

        executor.execute(() -> {}); // 유후 스레드 생성됨

        for (int i = 0; i < 3; i++) {
            executor.execute(blockingTask());
        }

        waitForActiveCount(executor, 3);

        testQueue.setExecutor(executor);

        // when
        boolean result = testQueue.offer(dummyTask);

        // then
        assertThat(result).isTrue();
        assertThat(testQueue.size()).isEqualTo(1);
    }

    @Test
    void offer_whenPoolIsEmpty_shouldReturnFalse() {
        // given
        // prestartAllCoreThreads() 미호출, 태스크 미제출
        // poolSize=0, activeCount=0, maxPoolSize=8
        executor = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        testQueue.setExecutor(executor);

        // when
        boolean result = testQueue.offer(dummyTask);

        // then
        // activeCount(0) == poolSize(0): 유휴 없음 분기
        // poolSize(0) < maxPoolSize(8): false 반환 -> 스레드 생성
        assertThat(result).isFalse();
        assertThat(testQueue.size()).isEqualTo(0);
    }


    @Test
    void forceOffer_shouldBypassOfferLogicAndEnqueue() throws InterruptedException {
        // given: offer()가 false를 반환할 조건 (유휴 없음 + max 미달)
        executor = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        executor.prestartAllCoreThreads();
        for (int i = 0; i < 4; i++) {
            executor.execute(blockingTask());
        }
        waitForActiveCount(executor, 4);
        testQueue.setExecutor(executor);

        // offer()가 실제로 false인지 먼저 확인
        assertThat(testQueue.offer(dummyTask)).isFalse();
        assertThat(testQueue.size()).isEqualTo(0);

        // when
        boolean result = testQueue.forceOffer(dummyTask);

        // then: 부모 LinkedBlockingQueue.offer()를 직접 호출하므로 항상 true
        assertThat(result).isTrue();
        assertThat(testQueue.size()).isEqualTo(1);
    }
}