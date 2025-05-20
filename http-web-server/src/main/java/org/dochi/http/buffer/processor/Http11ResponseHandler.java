package org.dochi.http.buffer.processor;

import org.dochi.inputbuffer.socket.SocketWrapperBase;
import org.dochi.webserver.config.HttpResConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Http11ResponseHandler extends AbstractResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(Http11ResponseHandler.class);

    public Http11ResponseHandler(HttpResConfig httpResConfig) {
        super(httpResConfig);
    }

//    // prevention using buffer for memory rapidly increment but maintain sending speed.
//    public void sendSplitFile(HttpStatus status, SplitFileResource splitFileResource) throws IOException {
//        addDefaultHeader(status, (int) splitFileResource.getFileSize(), splitFileResource.getContentType(null));
//        writeMessage(null);
//        int bytesRead;
//        final byte[] buffer = new byte[8192];
//        try(BufferedSocketInputStream in = splitFileResource.getInputStream()) {
//            // byte[]를 생성하는 대신 BufferedOutputStream 자식 클래스를 작성해서 buf 넘긴다.
//            while ((bytesRead = in.read(buffer)) != -1) {
//                try {
//                    bos.write(buffer, 0, bytesRead);
//                    bos.flush(); // 즉시 TCP 버퍼로 전달 (시스템 콜 비용 발생)
//                } catch (SocketException e) {
//                    log.debug("Send split file failed: {}", e.getMessage());
//                    throw e; // 네트워크 오류 전파
//                }
//            }
//        }
//    }


    @Override
    public void init(SocketWrapperBase<?> socketWrapper) {
//        this.inputBuffer.init(socketWrapper);
//        this.socketWrapper = socketWrapper;
        this.bos.init(socketWrapper);
    }

    protected void writeHeader() throws IOException {
        bos.write(String.format("%s %d %s\r\n", version.getVersion(), status.getCode(), status.getMessage()).getBytes(StandardCharsets.ISO_8859_1));
        Set<String> keys = headers.getHeaders().keySet();
        for (String key: keys) {
            String headerLine = key + ": " + headers.getHeaders().get(key) + "\r\n";
            bos.write(headerLine.getBytes(StandardCharsets.ISO_8859_1));
        }
        bos.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    protected void writePayload(byte[] body) throws IOException {
        if (body != null) {
            bos.write(body, 0, body.length);
        }
//        flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장
    }
}