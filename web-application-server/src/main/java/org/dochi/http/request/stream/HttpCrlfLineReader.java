package org.dochi.http.request.stream;

import org.dochi.http.monitor.MessageSizeMonitor;

import java.io.IOException;

// HTTP/1.1에서는 crlf 단위로 읽어서 멀티파트 폼 처리
// HTTP/2.0에서는 프레임 단위로 읽고 조립한 후에 crlf 단위로 끊어서 도달하면 라인으로 반환
public interface HttpCrlfLineReader {
    String readHeader(MessageSizeMonitor sizeMonitor) throws IOException;
    byte[] readLineBytes(MessageSizeMonitor sizeMonitor) throws IOException;
}
