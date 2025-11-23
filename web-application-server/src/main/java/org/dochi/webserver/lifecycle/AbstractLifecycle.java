package org.dochi.webserver.lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Lifecycle 전파 로직 기본 설정
// init -> start -> stop -> destroy
// fail시 rollback
// 여러 스레드에서 객체가 호출될수있으므로 상태 synchronized
// 1. WebAppServer 객체를 Main Class의 main method에서 start()만 호출해도 동시성 이슈 발생
//  -> 메인 스레드에서 Server.start()가 실행중인데, JVM의 셧다운훅 스레드가 실행되어 Server.stop() 실행
// 2. 사용자가 WebAppServer 객체를 공유 자원으로 사용하는 순간 동시성 이슈 발생
public abstract class AbstractLifecycle implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(AbstractLifecycle.class);

    public enum State {
        NEW,
        INITIALIZED,
        STARTED,
        STOPPED,
        DESTROYED
    }

    // 각 스레드는 자신만의 CPU 캐시를 사용할 수 있으며, 변수 값을 메인 메모리와 동기화하지 않고 캐시에 저장할수 있다.
    // 따라서 한 스레드에서 변경한 값이 다른 스레드에게 보이지 않는 현상인 Memory Visibility을 야기한다.
    // 이는 스레드 간 컨텍스트 스위칭이 발생하는 경우, 공유 자원에 접근할때 메모리 가시성 문제가 발생할 수 있다.
    private volatile State state = State.NEW;

    // 쓰기 시점에 배열을 복사: 라이프 사이클 실행중 리스트 변경 이슈 방지
    private final List<Lifecycle> lifecycles = new CopyOnWriteArrayList<>();

    private void setState(State newState) {
        state = newState;
        log.info("{} {}", getClass().getSimpleName(), state);
    }

    public final State getState() {
        return state;
    }

    @Override
    public void addLifecycle(Lifecycle lifecycle) {
        this.lifecycles.add(lifecycle);
    }

    @Override
    public void addLifecycle(int index, Lifecycle lifecycle) {
        this.lifecycles.add(index, lifecycle);
    }

    @Override
    public Lifecycle[] getLifecycles() {
        return lifecycles.toArray(new Lifecycle[0]);
    }

    protected interface LifecycleAction {
        void apply(Lifecycle lifecycle) throws LifecycleException;
    }

    protected void propagateLifecycles(LifecycleAction action) throws LifecycleException {
        for (Lifecycle lifecycle : lifecycles) {
            action.apply(lifecycle);
        }
    }

    @Override
    public final synchronized void init() throws LifecycleException {
        if (state != State.NEW || state == State.INITIALIZED) return;

        try {
            initInternal();
            setState(State.INITIALIZED);
        } catch (Exception e) {
            destroy();
            throw wrap(e, "init failed");
        }
    }

    @Override
    public final synchronized void start() throws LifecycleException {

        if (state == State.NEW) init();

        if (state != State.INITIALIZED || state == State.STARTED) return;

        try {
            startInternal();
            setState(State.STARTED);
        } catch (Exception e) {
            stop();
            throw wrap(e, "start failed");
        }
    }

    @Override
    public final synchronized void stop() throws LifecycleException {
        if (state != State.STARTED || state == State.STOPPED) return;

        try {
            stopInternal();
            setState(State.STOPPED);
        } catch (Exception e) {
            throw wrap(e, "stop failed");
        } finally {
            destroy();
        }
    }

    @Override
    public final synchronized void destroy() throws LifecycleException {
        if (state == State.STARTED) stop();

        if (state == State.DESTROYED) return;

        try {
            destroyInternal();
            setState(State.DESTROYED);
        } catch (Exception e) {
            throw wrap(e, "destroy failed");
        }
    }

    protected abstract void initInternal() throws LifecycleException;
    protected abstract void startInternal() throws LifecycleException;
    protected abstract void stopInternal() throws LifecycleException;
    protected abstract void destroyInternal() throws LifecycleException;

    private LifecycleException wrap(Exception e, String msg) {
        if (e instanceof LifecycleException le) return le;
        log.error(msg, e);
        return new LifecycleException(msg, e);
    }
}
