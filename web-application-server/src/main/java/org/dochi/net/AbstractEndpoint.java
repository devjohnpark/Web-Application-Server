package org.dochi.net;

import org.dochi.thread.ForceTaskQueuePolicy;
import org.dochi.thread.ScalableTaskQueue;
import org.dochi.webserver.config.SocketConfig;
import org.dochi.webserver.config.ThreadPoolConfig;
import org.dochi.webserver.lifecycle.AbstractLifecycle;
import org.dochi.webserver.lifecycle.LifecycleException;
import org.dochi.webserver.property.SocketProperty;
import org.dochi.webserver.property.ThreadPoolProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.*;

public abstract class AbstractEndpoint<S> extends AbstractLifecycle {
    private static final Logger log = LoggerFactory.getLogger(AbstractEndpoint.class);

    // 소켓 상태에 따라 프로토콜별 처리 핸들링 필요
    // Endpoint와 ProtocolHandler 사이의 접점 하나로 묶어 둔 인터페이스
    // Endpoint: Connection Handler
    // ProtocolHandler: protocol Handler
    public interface Handler<S> {
        SocketState process(AbstractSocketWrapper<S> socket);
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
    protected SocketConfig socketConfig;
    protected Deque<AbstractSocketTask<S>> socketTaskPool;
    private ThreadPoolConfig threadPoolConfig;
    private boolean bound = false;
    private CountDownLatch keepAliveLatch;

    public AbstractEndpoint(int port, String hostName) {
        this.port = port;
        this.hostName = hostName;
    }

    protected abstract void bind() throws IOException;
    protected abstract S serverSocketAccept() throws IOException;

    protected boolean processSocketTask(S socket) {
        AbstractSocketTask<S> socketTask = socketTaskPool.pollFirst();
        if (socketTask != null) {
            socketTask.reset(wrapSocket(socket));
        } else {
            socketTask = createSocketTask(wrapSocket(socket));
        }
        try {
            executor.execute(socketTask);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return false;
        }
        return true;
    }

    protected abstract AbstractSocketTask<S> createSocketTask(AbstractSocketWrapper<S> socketWrapper);

    protected abstract AbstractSocketWrapper<S> wrapSocket(S socket);

    protected void closeSocket(S socket) {
        AbstractSocketWrapper<S> socketWrapper = wrapSocket(socket);
        if (socketWrapper.isClosed()) {
            socketWrapper.close();
        }
    }

    protected abstract void closeServerSocket() throws IOException;

    @Override
    protected void initInternal() throws LifecycleException {
        this.acceptor = new Acceptor<>(this);
        doBind();
        createSocketConfig();
        createThreadPoolConfig();
        createSocketTaskCache();
    }

    private void doBind() throws LifecycleException {
        if (bound) return;
        try {
            this.bind();
            bound = true;
            log.info("Server bound to {}:{}", hostName, port);
        } catch (IOException e) {
            throw new LifecycleException("Failed to bind port: " + port + ", hostName: " + hostName, e);
        }
    }

    @Override
    protected void startInternal() throws LifecycleException {
        createExecutor();
        acceptorThread = new Thread(acceptor, "acceptor");
        acceptorThread.setDaemon(false);
        acceptorThread.start();
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        try {
            if (acceptor != null) acceptor.close();
            shutdownExecutor();
        } catch (IOException e) {
            throw new LifecycleException("Failed to stop web server", e);
        }
    }

    @Override
    protected void destroyInternal() throws LifecycleException {
        unbound();
    }

    private void unbound() {
        bound = false;
        log.info("Server unbound to {}:{}", hostName, port);
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

    public void setThreadPoolConfig(ThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
    }

    protected void createThreadPoolConfig() {
        if (threadPoolConfig != null) return;
        threadPoolConfig = new ThreadPoolProperty();
    }

    private void createSocketConfig() {
        if (socketConfig != null) return;
        socketConfig = new SocketProperty();
    }

    private void createExecutor() {
        if (executor != null) return;

        if (threadPoolConfig.getUseVirtualThreads()) {
            // Virtual thread는 실행될 때 carrier(platform) thread에 mount 되고, JVM이 인식할 수 있는 blocking I/O시 unmount 된다.
            // 따라서 java.net.Socket은 blockng I/O지만, virtual thread를 사용하면 non-blocking 방식으로 처리될수있다.
            // Heap에 수많은 Virtual Thread를 할당해놓고 platform thread에 대상 Virtual Thread를 마운트/언마운트하여 컨텍스트 스위칭을 수행한다. 따라서 컨텍스트 스위칭 비용이 작아질 수 밖에 없다.

            // 다만, synchronized 내부에서 blocking되면 virtual thread는 platform thread와 unmount 될수 없다.
            /*
            synchronized(lock) {
                socket.read();  // blocking
            }
             */
            // user thread인 platform thread는 kernel thread와 1:1 맵핑
            // synchronized는 JVM monitor lock을 사용하며, monitor는 OS thread(platform thread) 기반이다.
            // 따라서 platform thread가 모니터중에 virtual thread하고 unmount 될수가 없는 것이다.

            // Dochi WAS 요청 처리 내부 로직은 synchronized를 사용하지 않았기에 성능이 많이 향상되었다.
            // 그러나 spring 로직 내에서는 synchronized를 사용해서 효율이 좋지 않을 것이다. 따라서 디폴트로 virtual thread 사용을 false로 해놓는다.

            this.executor = Executors.newVirtualThreadPerTaskExecutor();
            // Virtual Thread 사용 시 jvm 종료 방지용 non daemon 스레드 추가
            keepAliveLatch = new CountDownLatch(1);
            Thread thread = new Thread(() -> {
                try {
                    keepAliveLatch.await(); // stopInternal()이 호출될 때까지 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "virtual-thread_keep-alive");
            thread.setDaemon(false);
            thread.start();

            log.info("{} STARTED.", executor.getClass().getSimpleName());
        } else {
            // ThreadPoolExecutor는 제출된 하나의 태스크를 단 하나의 워커 스레드에만 할당한다.
            // 소켓 작업은 하나의 워커 스레드에 할당되므로 소켓 작업 내에서 참조하는 객체의 동시성 문제는 없고 메모리 가시성만 신경 쓰면된다.
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    threadPoolConfig.getMinSpareThreads(),
                    threadPoolConfig.getMaxThreads(),
                    60L, // corePoolSize을 초과하는 스레드가 할당된 작업이 없는 경우 keepAliveTime이 경과한 뒤 제거
                    TimeUnit.SECONDS,
                    new ScalableTaskQueue(),
                    new ForceTaskQueuePolicy()
            );

            ScalableTaskQueue queue = (ScalableTaskQueue) executor.getQueue();

            queue.setExecutor(executor);

            executor.prestartAllCoreThreads(); // non demon thread(user thread) 이므로 was 인스턴스 실행 후 종료되지 않음

            log.info("{} STARTED. [poolSize={}, active={}, queued={}]",
                    executor.getClass().getSimpleName(),
                    executor.getPoolSize(),
                    executor.getActiveCount(),
                    executor.getQueue().size());

            this.executor = executor;
        }
    }

    protected void createSocketTaskCache() {
        if (socketTaskPool != null) return;
        this.socketTaskPool = new ConcurrentLinkedDeque<>();
    }

    protected void shutdownExecutor() {
        if (executor == null) return;
        if (executor instanceof ThreadPoolExecutor threadPoolExecutor) {
            log.info("Shutdown {}. [poolSize={}, active={}, queued={}]",
                    threadPoolExecutor.getClass().getSimpleName(),
                    threadPoolExecutor.getPoolSize(),
                    threadPoolExecutor.getActiveCount(),
                    threadPoolExecutor.getQueue().size());
            threadPoolExecutor.shutdownNow();
            ScalableTaskQueue queue = (ScalableTaskQueue) threadPoolExecutor.getQueue();
            queue.setExecutor(null);
            try {
                // Executor가 종료될때까지 5초간 대기 (true를 반환하면 모든 작업이 종료됨었음을 의미)
                threadPoolExecutor.awaitTermination(3, TimeUnit.SECONDS);
                if (threadPoolExecutor.isTerminated()) {
                    log.info("{} STOPPED.",
                            threadPoolExecutor.getClass().getSimpleName());
                }
            } catch (InterruptedException e) {
                // await 메서드 호출 대기 중에 인터럽트 신호를 받으면, 즉시 깨어나면서 InterruptedException을 던진다
                // shutdownExecutor 메서드를 호출한 스레드에 대해 다른 스레드가 thread.interrupt() 호출하면 발생
                // 종료 중 인터럽트는 무시
            }
        } else if (executor instanceof ExecutorService executorService) {
            // keep-alive 스레드 해제
            if (keepAliveLatch != null) {
                keepAliveLatch.countDown(); // await() 블로킹 해제 -> 스레드 종료
            }
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(3, TimeUnit.SECONDS);
                if (executorService.isTerminated()) {
                    log.info("{} STOPPED. ",
                            executorService.getClass().getSimpleName());
                }
            } catch (InterruptedException e) {
                // 종료 중 인터럽트 무시
            }
        }
    }
}
