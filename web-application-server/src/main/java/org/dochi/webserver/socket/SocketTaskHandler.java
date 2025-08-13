package org.dochi.webserver.socket;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.processor.Http11Processor;
import org.dochi.http.processor.HttpProcessor;
import org.dochi.http.response.HttpStatus;
import org.dochi.webserver.config.HttpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

// BioSocketWrapper(Socket)과 HttpApiMapper 객체를 가지고 클라이언트의 요청 프로세싱
public class SocketTaskHandler implements SocketTask {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskHandler.class);
    private final SocketWrapper socketWrapper;
    private final HttpApiMapper httpApiMapper; // 동적 HTTP API 추가를 위해 웹서버 인스턴스의 싱글톤 Container 주입
    private final HttpConfig httpConfig;

    public SocketTaskHandler(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper, HttpConfig httpConfig) {
        this.socketWrapper = socketWrapper;
        this.httpApiMapper = httpApiMapper;
        this.httpConfig = httpConfig;
    }

    // 1. 소켓 읽기 타임 아웃 초과시 읽기나 쓰기 하면 예외 발생
    // 2. 그렇다면 소켓 읽기 타임 아웃 초과했다면 대기 큐에 스레드 남아있어서 리소스 낭비됨
    // 4. 요청을 처리할려다 timeout 초과 408 예외를 보내서 클라이언트가 다시 요청을 보낼수 있도록 한다.

    // 1. 스레드에 유효 시간을 부여
    // 2. 클라이언트 연결 소켓에 setSoTimeout으로 읽기 시간 설정 -> 연결 소켓 모니터링 -> getSoTimeout 호출 0이면 제거
    @Override
    public void run() {
        try {
            handleSocketTask(socketWrapper.getSocket());
        } catch (RuntimeException e) {
            log.error("Socket RuntimeException: {}", e.getMessage());
        }
    }


    // 프로토콜 판별 -> Http11Processor 생성 -> Http11Processor 객체 생성시, Request, HttpApiRequest 객체 초기화
    // HttpXXProcessor 객체를 미리 생성할순 없을까? -> 기본 개수만 생성하고 초과하면 동적으로 2배씩 확장? or 동적으로 하나씩 생성하고 재활용
    // AbstractProtocol 클래스에서 SynchronizedStack 객체를 사용해서 HttpXXProcessor 객체를 push(), pop()
    // AbstractProcessor 클래스도 AbstractProtocol 클래스에서 recycle를 통해 초기화해서 재활용
    // AbstractProcessor을 재활용하기 위해서 해당 클래스에 setSocketWrapper(BioSocketWrapper) 메서드를 정의하였다.
    // 그리고 HttpXXInputBuffer 재홀용하기 위해서 해당 클래스에 init(BioSocketWrapper) 메서드를 정의하였다.
    private void handleSocketTask(Socket socket) {
        log.debug("Running socket task connected to a client [Client: {}, Port: {}]",
                socket.getInetAddress(), socket.getPort());

        // SocketWrapper 내부로 I/O Stream 숨기기
        try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream();) {

            // 추후 ProcessorHandler 객체로 처리
            // HttpProcessor을 재활용하기 위해서는 네트워크 입출력 객체를 setter로 주입해야한다.
            // BioSocketWrapper를 생성해서 Http
            HttpProcessor httpProcessor = new Http11Processor(in, out, httpConfig);

            socketWrapper.startConnectionTimeout(socketWrapper.getKeepAliveTimeout());

            httpProcessor.process(socketWrapper, httpApiMapper);


//            SocketState state = httpProcessor.process(socketWrapper, httpApiMapper);


//            if (httpProcessor.process(socketWrapper, httpApiMapper) == SocketState.UPGRADED) {
//                1. 파싱된 요청 데이터 객체의 복사본을 가지고 헤더에서 h2 관련 데이터 가져와서 HTTP/2 설정
//                2. 프로토콜 업그레이드 요청시 프로세서 변경 후 소켓 입출력 스트림 주입하고 process 메서드 호출
//                httpProcessor = new Http2Processor(in, out, httpConfig);
//                httpProcessor.process(socketWrapper, httpApiMapper);
//                log.debug("Upgrading socket [Client: {}, Port: {}]", socket.getInetAddress(), socket);
//            }

        } catch (SocketException e) {
            log.error("Set connection timeout but socket is already closed: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Socket get stream error: {} [Client: {}, Port: {}]",
                    socket.getInetAddress(), socket.getPort(), e.getMessage());
        } finally {
            close(socket);
        }
    }

    private void close(Socket socket) {
        try {
            if (!socket.isClosed()) {
                log.debug("try socket closed");
                socket.close();
                log.debug("Socket closed [Client: {}, Port: {}]",
                        socket.getInetAddress(), socket.getPort());
            }
        } catch (IOException e) {
            log.error("Failed to close socket: {}", e.getMessage());
        }
    }

    @Override
    public SocketWrapper getSocketWrapper() {
        return socketWrapper;
    }
}
