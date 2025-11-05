package org.dochi.webserver.net;

import org.dochi.webserver.attribute.SocketAttribute;
import org.dochi.webserver.attribute.ThreadPoolAttribute;
import org.dochi.webserver.config.SocketConfig;
import org.dochi.webserver.config.ThreadPoolConfig;
import org.dochi.webserver.lifecycle.Lifecycle;
import org.dochi.webserver.lifecycle.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.*;

public abstract class EndpointBase<S> implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(EndpointBase.class);

    // 소켓 상태에 따라 프로토콜별 처리 핸들링 필요
    // Endpoint와 ProtocolHandler 사이의 접점 하나로 묶어 둔 인터페이스
    // Endpoint: Connection Handler
    // ProtocolHandler: protocol Handler
    public interface Handler<S> {
        SocketState process(SocketWrapperBase<S> socket);
        enum SocketState {
            OPEN,
            CLOSED,
            UPGRADING,
            UPGRADED,
        }
    }

    protected final int port;
    protected final String hostName;
    protected Acceptor<S> acceptor;
    protected Thread acceptorThread;
    protected Executor executor;
    protected Handler<S> handler;
    private ThreadPoolConfig threadPoolConfig;
    private SocketConfig socketConfig;
    protected Deque<SocketTaskBase<S>> socketTaskCache;

    public EndpointBase(int port, String hostName) {
        this.port = port;
        this.hostName = hostName;
    }

    protected abstract void bind() throws IOException;
    protected abstract S serverSocketAccept() throws IOException;

    protected boolean processSocket(S socket) {
        try {
            executor.execute(() -> {
                SocketTaskBase<S> socketTask = socketTaskCache.pollFirst();
                if (socketTask != null) {
                    socketTask.reset(wrapSocket(socket));
                } else {
                    socketTask = createSocketTask(wrapSocket(socket));
                }
                try {
                    socketTask.run();
                } finally {
                    socketTaskCache.addFirst(socketTask);
                }
            });
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return false;
        }
        return true;
    }

    protected abstract SocketTaskBase<S> createSocketTask(SocketWrapperBase<S> socketWrapper);

    protected abstract SocketWrapperBase<S> wrapSocket(S socket);

    protected void closeSocket(S socket) {
        SocketWrapperBase<S> socketWrapper = wrapSocket(socket);
        if (socketWrapper.isClosed()) {
            socketWrapper.close();
        }
    }

    protected abstract void closeServerSocket() throws IOException;

    @Override
    public void init() throws LifecycleException {
        this.acceptor = new Acceptor<>(this);
        createThreadPoolConfig();
        createSocketConfig();
        createExecutor();
        createSocketTaskCache();
        log.info("{} init ",
                getClass().getSimpleName());
    }

    @Override
    public void start() throws LifecycleException {
        acceptorThread = new Thread(acceptor, "acceptor");
        acceptorThread.setDaemon(true);
        acceptorThread.start();
        log.info("{} start ",
                getClass().getSimpleName());
    }

    @Override
    public void stop() throws LifecycleException {
        try {
            if (acceptor != null) acceptor.close();
            shutdownExecutor();
        } catch (IOException e) {
            throw new LifecycleException("Failed to stop web server", e);
        }
        log.info("{} stop ",
                getClass().getSimpleName());
    }

    @Override
    public void destroy() throws LifecycleException {
        log.info("{} destroy ",
                getClass().getSimpleName());
    }

    public void setExecutor(Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor cannot be null");
        }
        this.executor = executor;
    }

    public void setHandler(Handler<S> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }
        this.handler = handler;
    }

    public void setSocketConfig(SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
    }

    public SocketConfig getSocketConfig() {
        return socketConfig;
    }

    public void setThreadPoolConfig(ThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
    }

    public ThreadPoolConfig getThreadPoolConfig() {
        return threadPoolConfig;
    }

    protected void createSocketConfig() {
        if (socketConfig != null) return;
        this.socketConfig = new SocketAttribute();
    }

    protected void createThreadPoolConfig() {
        if (threadPoolConfig != null) return;
        this.threadPoolConfig = new ThreadPoolAttribute();
    }

    protected void createExecutor() {
        if (executor != null) return;
        if (threadPoolConfig == null) createThreadPoolConfig();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threadPoolConfig.getMinSpareThreads(),
                threadPoolConfig.getMaxThreads(),
                60L, // corePoolSize을 초과하는 스레드가 할당된 작업이 없는 경우 keepAliveTime이 경과한 뒤 제거
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );

        executor.prestartCoreThread();

        log.info("{} init. [poolSize={}, active={}, queued={}]",
                executor.getClass().getSimpleName(),
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size());

        this.executor = executor;
    }

    protected void createSocketTaskCache() {
        if (socketTaskCache != null) return;
        this.socketTaskCache = new ConcurrentLinkedDeque<>();
    }

    protected void shutdownExecutor() {
        if (executor != null) {
            if (executor instanceof ThreadPoolExecutor threadPoolExecutor) {
                // 새로운 작업 수락 중지
                threadPoolExecutor.shutdown();
                try {
                    // 진행 중인 모든 작업이 완료될 때까지 5초간 대기 (true를 반환하면 모든 작업이 종료됨었음을 의미
                    if (!threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        threadPoolExecutor.shutdownNow(); // 5초 후에도 종료되지 않은 경우 즉시 모든 작업 종료
                    }
                    if (!threadPoolExecutor.isTerminated()) {
                        log.warn("Thread pool executor wasn't terminated");
                    }
                } catch (InterruptedException e) { // await 메서드 호출 대기 중에 인터럽트 신호를 받으면, 즉시 깨어나면서 InterruptedException을 던진다
                    // shutdownExecutor 메서드를 호출한 스레드에 대해 다른 스레드가 thread.interrupt() 호출하면 발생
                    // 종료 중 인터럽트는 무시
                    log.warn("ThreadPoolExecutor shutdown has interrupted.", e);
                }
            }
        }
    }
}
