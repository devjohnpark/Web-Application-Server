package org.dochi.webserver.protocol;

import org.dochi.internal.processor.Http11Processor;
import org.dochi.internal.processor.HttpProcessor;
import org.dochi.webserver.config.HttpConfig;

import java.util.concurrent.ConcurrentLinkedDeque;

public class HttpProtocolHandler {
    private final HttpConfig config;
    private final ConcurrentLinkedDeque<Http11Processor> http11Pool;

    public HttpProtocolHandler(HttpConfig config) {
        this.config = config;
        // 락 프리(lock-free) 알고리즘을 사용하여 높은 동시성 성능: CAS(Compare-And-Swap) 연산으로 CAS는 CPU가 메모리 위치(V)의 현재 값을 읽어서 메모리 값을 기대 값과 비교해 일치하면 새 값으로 원자적으로 교체하는 동시성 기법
        // Deque 구현체로 LIFO 방식으로 사용할 수 있음 (addFirst로 추가, pollFirst로 제거) -> 최근에 사용된 객체를 우선 재사용하여 캐시 지역성을 활용
        this.http11Pool = new ConcurrentLinkedDeque<>();
    }

    // HTTPS (TLS 사용): ALPN으로 초기부터 HTTP/2 선택
    // HTTP (plain TCP): Upgrade 헤더로 HTTP/1.1에서 전환 -> Http11Processor/Http2Processor 풀 따로 저장

    public HttpProcessor getProcessor() {
       return getProcessor("HTTP/1.1");
    }

    public HttpProcessor getProcessor(String protocolName) {
        Http11Processor processor = null;
        if (protocolName.equals("HTTP/1.1")) {
            processor = http11Pool.pollFirst(); // pop()은 비어있을 때 예외를 발생시키므로 pollFirst() 사용
        }
        return processor != null ? processor : createProcessor(protocolName);
    }

    private HttpProcessor createProcessor(String protocolName) {
        if (protocolName.equals("HTTP/1.1")) {
            return new Http11Processor(config);
        }
        throw new IllegalArgumentException("Unknown protocol: " + protocolName);
    }

    public void release(HttpProcessor processor) {
        // (instanceof는 JVM에서 최적화됨)
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
