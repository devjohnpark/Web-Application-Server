package org.dochi.webserver.lifecycle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import org.dochi.webserver.lifecycle.AbstractLifecycle.State;

class AbstractLifecycleTest {

    static class TestLifecycle extends AbstractLifecycle {
        int initCount;
        int startCount;
        int stopCount;
        int destroyCount;

        LifecycleException initError;
        LifecycleException startError;
        LifecycleException stopError;
        LifecycleException destroyError;

        @Override protected void initInternal() throws LifecycleException {
            initCount++;
            if (initError != null) throw initError;
        }

        @Override protected void startInternal() throws LifecycleException {
            startCount++;
            if (startError != null) throw startError;
        }

        @Override protected void stopInternal() throws LifecycleException {
            stopCount++;
            if (stopError != null) throw stopError;
        }

        @Override protected void destroyInternal() throws LifecycleException {
            destroyCount++;
            if (destroyError != null) throw destroyError;
        }
    }

    private TestLifecycle lifecycle;

    @BeforeEach
    void setup() {
        lifecycle = new TestLifecycle();
    }

    @Test
    void initial_state_is_NEW() {
        assertThat(lifecycle.getState()).isEqualTo(State.NEW);
        assertThat(lifecycle.initCount).isZero();
        assertThat(lifecycle.startCount).isZero();
        assertThat(lifecycle.stopCount).isZero();
        assertThat(lifecycle.destroyCount).isZero();
    }

    @Test
    void init_from_NEW_transitions_to_INITIALIZED() throws Exception {
        lifecycle.init();

        assertThat(lifecycle.getState()).isEqualTo(State.INITIALIZED);
        assertThat(lifecycle.initCount).isEqualTo(1);
        assertThat(lifecycle.startCount).isZero();
        assertThat(lifecycle.stopCount).isZero();
        assertThat(lifecycle.destroyCount).isZero();
    }

    @Test
    void init_is_noop_when_not_NEW() throws Exception {
        lifecycle.init();
        lifecycle.init();

        assertThat(lifecycle.getState()).isEqualTo(State.INITIALIZED);
        assertThat(lifecycle.initCount).isEqualTo(1);
    }

    @Test
    void start_from_NEW_calls_init_then_start_and_transitions_to_STARTED() throws Exception {
        lifecycle.start();

        assertThat(lifecycle.getState()).isEqualTo(State.STARTED);
        assertThat(lifecycle.initCount).isEqualTo(1);
        assertThat(lifecycle.startCount).isEqualTo(1);
        assertThat(lifecycle.stopCount).isZero();
        assertThat(lifecycle.destroyCount).isZero();
    }

    @Test
    void start_from_INITIALIZED_calls_start_and_transitions_to_STARTED() throws Exception {
        lifecycle.init();
        lifecycle.start();

        assertThat(lifecycle.getState()).isEqualTo(State.STARTED);
        assertThat(lifecycle.initCount).isEqualTo(1);
        assertThat(lifecycle.startCount).isEqualTo(1);
    }

    @Test
    void start_is_noop_when_state_is_STARTED() throws Exception {
        lifecycle.start();
        int startCountAfterFirstStart = lifecycle.startCount;

        lifecycle.start(); // STARTED 상태에서 호출

        // STARTED면 (state != INITIALIZED) 이라 return -> startInternal 재호출 안 됨
        assertThat(lifecycle.getState()).isEqualTo(State.STARTED);
        assertThat(lifecycle.startCount).isEqualTo(startCountAfterFirstStart);
    }

    @Test
    void start_is_noop_when_state_is_STOPPED_or_DESTROYED() throws Exception {
        lifecycle.start();
        lifecycle.stop(); // DESTROYED까지 감

        assertThat(lifecycle.getState()).isEqualTo(State.DESTROYED);

        int initCount = lifecycle.initCount;
        int startCount = lifecycle.startCount;

        lifecycle.start(); // DESTROYED에서 start 호출은 내부적으로 init 안 함(NEW가 아님), state != INITIALIZED 이므로 return

        assertThat(lifecycle.getState()).isEqualTo(State.DESTROYED);
        assertThat(lifecycle.initCount).isEqualTo(initCount);
        assertThat(lifecycle.startCount).isEqualTo(startCount);
    }

    @Test
    void stop_is_noop_when_state_is_NEW() throws Exception {
        lifecycle.stop();

        assertThat(lifecycle.getState()).isEqualTo(State.NEW);
        assertThat(lifecycle.stopCount).isZero();
        assertThat(lifecycle.destroyCount).isZero();
    }

    @Test
    void stop_from_STARTED_transitions_to_DESTROYED_and_calls_destroy_in_finally() throws Exception {
        lifecycle.start();
        lifecycle.stop();

        // stopInternal 1회 + destroyInternal 1회
        assertThat(lifecycle.stopCount).isEqualTo(1);
        assertThat(lifecycle.destroyCount).isEqualTo(1);

        // stop() finally -> destroy()까지 호출되므로 최종 DESTROYED
        assertThat(lifecycle.getState()).isEqualTo(State.DESTROYED);
    }

    @Test
    void stop_is_noop_when_state_is_INITIALIZED() throws Exception {
        lifecycle.init();
        lifecycle.stop();

        assertThat(lifecycle.getState()).isEqualTo(State.INITIALIZED);
        assertThat(lifecycle.stopCount).isZero();
        assertThat(lifecycle.destroyCount).isZero();
    }

    @Test
    void destroy_is_noop_when_state_is_NEW() throws Exception {
        lifecycle.destroy();

        // 구현상 NEW면 return (destroyInternal 호출 안 함)
        assertThat(lifecycle.getState()).isEqualTo(State.NEW);
        assertThat(lifecycle.destroyCount).isZero();
    }

    @Test
    void destroy_from_INITIALIZED_transitions_to_DESTROYED() throws Exception {
        lifecycle.init();
        lifecycle.destroy();

        assertThat(lifecycle.destroyCount).isEqualTo(1);
        assertThat(lifecycle.getState()).isEqualTo(State.DESTROYED);
    }

    @Test
    void destroy_from_STARTED_calls_stop_first_then_destroy_and_ends_in_DESTROYED() throws Exception {
        lifecycle.start();
        lifecycle.destroy();

        // destroy()가 STARTED면 stop() 먼저 -> stopInternal 1회
        // stop() finally에서 destroy() 호출 -> destroyInternal 1회
        // 바깥 destroy()는 stop()이 이미 DESTROYED까지 만들었으므로
        // if (state == DESTROYED) return 에 걸려 destroyInternal 추가 호출 없음
        assertThat(lifecycle.stopCount).isEqualTo(1);
        assertThat(lifecycle.destroyCount).isEqualTo(1);
        assertThat(lifecycle.getState()).isEqualTo(State.DESTROYED);
    }

    @Test
    void destroy_is_noop_when_state_is_DESTROYED() throws Exception {
        lifecycle.start();
        lifecycle.stop(); // DESTROYED

        int destroyCount = lifecycle.destroyCount;

        lifecycle.destroy();

        assertThat(lifecycle.getState()).isEqualTo(State.DESTROYED);
        assertThat(lifecycle.destroyCount).isEqualTo(destroyCount);
    }

    @Test
    void addLifecycle_appendsInOrder() {
        TestLifecycle child1 = new TestLifecycle();
        TestLifecycle child2 = new TestLifecycle();

        lifecycle.addLifecycle(child1);
        lifecycle.addLifecycle(child2);

        assertThat(lifecycle.getLifecycles()).containsExactly(child1, child2);
    }

    @Test
    void addLifecycle_withIndex_insertsAtCorrectPosition() {
        TestLifecycle child1 = new TestLifecycle();
        TestLifecycle child2 = new TestLifecycle();
        TestLifecycle child3 = new TestLifecycle();

        lifecycle.addLifecycle(child1);
        lifecycle.addLifecycle(child2);
        lifecycle.addLifecycle(1, child3);

        assertThat(lifecycle.getLifecycles()).containsExactly(child1, child3, child2);
    }

    @Test
    void propagateLifecycles_callsActionOnAllChildrenInOrder() throws Exception {
        TestLifecycle child1 = new TestLifecycle();
        TestLifecycle child2 = new TestLifecycle();

        lifecycle.addLifecycle(child1);
        lifecycle.addLifecycle(child2);

        lifecycle.propagateLifecycles(Lifecycle::init);

        assertThat(child1.initCount).isEqualTo(1);
        assertThat(child2.initCount).isEqualTo(1);
    }

    @Test
    void propagateLifecycles_stopsAndThrows_whenChildFails() throws Exception {
        TestLifecycle failingChild = new TestLifecycle();
        TestLifecycle nextChild = new TestLifecycle();
        failingChild.initError = new LifecycleException("init failed");

        lifecycle.addLifecycle(failingChild);
        lifecycle.addLifecycle(nextChild);

        assertThatThrownBy(() -> lifecycle.propagateLifecycles(Lifecycle::init))
                .isInstanceOf(LifecycleException.class);

        assertThat(nextChild.initCount).isZero(); // 예외 후 나머지 미실행
    }
}