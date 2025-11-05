package org.dochi.internal;

import org.dochi.connector.Adapter;
import org.dochi.internal.http11.Http11Processor;
import org.dochi.webserver.config.SocketConfig;
import org.dochi.webserver.config.ThreadPoolConfig;
import org.dochi.webserver.net.EndpointBase;
//import org.dochi.webserver.Handler;
import org.dochi.webserver.config.HttpConfig;
//import org.dochi.webserver.socket.SocketState;
import org.dochi.webserver.lifecycle.Lifecycle;
import org.dochi.webserver.lifecycle.LifecycleException;
import org.dochi.webserver.net.SocketWrapperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;

import static org.dochi.webserver.net.EndpointBase.Handler.SocketState.*;

public class HttpProtocolHandler implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(HttpProtocolHandler.class);
    private final EndpointBase<?> endpoint;
    private Adapter adapter;
    private HttpConfig config;

    public HttpProtocolHandler(EndpointBase<?> endpoint) {
        this.endpoint = endpoint;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    public void setHttpConfig(HttpConfig config) {
        this.config = config;
    }

    public void setSocketConfig(SocketConfig socketConfig) {
        this.endpoint.setSocketConfig(socketConfig);
    }

    public void setThreadPoolConfig(ThreadPoolConfig threadPoolConfig) {
        this.endpoint.setThreadPoolConfig(threadPoolConfig);
    }


    // HTTPS (TLS 사용): ALPN으로 초기부터 HTTP/2 선택
    // HTTP (plain TCP): Upgrade 헤더로 HTTP/1.1에서 전환 -> Http11Processor/Http2Processor 풀 따로 저장

    @Override
    public void init() throws LifecycleException {
        log.info("{} init ",
                getClass().getSimpleName());
        this.endpoint.setHandler(new ConnectionHandler<>());
        this.endpoint.init();
    }

    @Override
    public void start() throws LifecycleException {
        log.info("{} start ",
                getClass().getSimpleName());
        this.endpoint.start();
    }

    @Override
    public void stop() throws LifecycleException {
        this.endpoint.stop();
        log.info("{} stop ",
                getClass().getSimpleName());
    }

    @Override
    public void destroy() throws LifecycleException {
        this.endpoint.destroy();
        log.info("{} destroy ",
                getClass().getSimpleName());
    }

    protected class ProcessorRecycler {
        // 락 프리(lock-free) 알고리즘을 사용하여 높은 동시성 성능: CAS(Compare-And-Swap) 연산으로 CAS는 CPU가 메모리 위치(V)의 현재 값을 읽어서 메모리 값을 기대 값과 비교해 일치하면 새 값으로 원자적으로 교체하는 동시성 기법
        // Deque 구현체로 LIFO 방식으로 사용할 수 있음 (addFirst로 추가, pollFirst로 제거) -> 최근에 사용된 객체를 우선 재사용하여 캐시 지역성을 활용
        private final ConcurrentLinkedDeque<Http11Processor> http11Pool = new ConcurrentLinkedDeque<>();

        public HttpProcessor getProcessor() {
            return getProcessor("HTTP/1.1");
        }

        public HttpProcessor getProcessor(String protocolName) {
            HttpProcessorBase processor = null;
            if (protocolName.equals("HTTP/1.1")) {
                processor = http11Pool.pollFirst(); // pop()은 비어있을 때 예외를 발생시키므로 pollFirst() 사용
            }
            return processor != null ? processor : createProcessor(protocolName);
        }

        private HttpProcessor createProcessor(String protocolName) {
            if (protocolName.equals("HTTP/1.1")) {
                return new Http11Processor(adapter, config);
            }
            throw new IllegalArgumentException("Unknown protocol: " + protocolName);
        }

        public void release(HttpProcessor processor) {
            if (processor instanceof Http11Processor) {
                // 맨 앞에 추가하여 LIFO 방식으로 관리
                http11Pool.addFirst((Http11Processor) processor);
            } else {
                throw new IllegalArgumentException("Unknown processor: " + processor.getClass().getName());
            }
        }

        public int getSize(String protocolName) {
            if (protocolName.equals("HTTP/1.1")) {
                return http11Pool.size();
            }
            throw new IllegalArgumentException("Unknown protocol: " + protocolName);
        }
    }

    protected class ConnectionHandler<S> implements EndpointBase.Handler<S> {
        private final ProcessorRecycler processorRecycler;

        public ConnectionHandler() {
            this.processorRecycler = new ProcessorRecycler();
        }

        @Override
        public SocketState process(SocketWrapperBase<S> socket) {
            SocketState state = OPEN;
            HttpProcessor processor = processorRecycler.getProcessor();
            try {
                while (state == OPEN) {
                    state = processor.process(socket);
                    if (state == CLOSED) {
                        release(processor);
                        processor = null;
                    }
//                    else if (state == UPGRADING) {
//                        // 1. 파싱된 요청 데이터 객체(internal.RequestHeader)의 복사본을 가지고 헤더에서 h2 관련 데이터 가져와서(AbstractProcessor.getUpgradeToken()) HTTP/2 설정
//                        // 2. 필요한 스트림의 개수 만큼 Http2Processor 생성
//                        // 3. 소켓 연결 시간 다시 설정
//
//                        // HTTP/2.0은 다수의 스트림을 필요하므로 다수의 HttpProcessor가 필요하다.
//                        // 땨라서 반복문으로 필요한 스트림 개수만큼 HttpProcessor 생성
//                        // processor = getProcessor("HTTP/2.0");
//                        // HttpProcessor.process() 비동기로 전환 필요
//                    }
                }
            } catch (Throwable t) {
                log.error("Error processing connection", t);
            } finally {
                if (processor != null) {
                    release(processor);
                }
            }
            return state;
        }

        private void release(HttpProcessor processor) {
            processor.recycle();
            processorRecycler.release(processor);
        }
    }
}
