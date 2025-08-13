package org.dochi.http.api;

import org.dochi.http.request.data.HttpMethod;
import org.dochi.http.response.HttpStatus;
import org.dochi.webresource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DefaultHttpApiHandler extends AbstractHttpApiHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultHttpApiHandler.class);

    @Override
    public void service(HttpApiRequest request, HttpApiResponse response) throws IOException {
        if (HttpMethod.GET.equals(request.getMethod())) {
            doGet(request, response);
        } else {
            super.service(request, response);
        }
    }

    @Override
    public void doGet(HttpApiRequest request, HttpApiResponse response) throws IOException {
        Resource resource = webResourceProvider.getResource(request.getPath());
        if (!resource.isEmpty()) {
            response.send(HttpStatus.OK, resource.getData(), resource.getContentType("UTF-8"));
        } else {
            response.sendError(HttpStatus.NOT_FOUND);
        }
    }

//    @Override
//    public void doGet(HttpApiRequest request, Http11ResponseProcessor response) throws IOException {
//        // TCP (Transmission Control Protocol)는 스트리밍 프로토콜이다.
//        // 그 이유는 HTTP 메세지를 TCP에서 패킷단위로 분할되어 전송되지만, 수신측 OS단에서 순서대로 조립한다.
//
//        // 본문을 분할전송해도 되는지?
//        // 본문은 Content-Length로 크기를 인지해서 해당 헤더 필드 값만큼 읽으면된다.
//        // 수신측(브라우저, 애플리케이션0에서 본문을 Content-Length 크기만큼 읽도록 동작한다면, 본문을 분할해서 전송가능하다.
//
//        // 본문 분할 전송 동작
//        // 1) Payload를 분할해서 전송하기전에 헤더를 먼저 보낸다.
//        // 2) 파일을 읽기전에 파일 용량만 알아내서 Content-Length 헤더 필드 값을 설정한다.
//        // 3) request.path()로 ResourceType로 Content-Type 헤더 필드 값을 설정한다.
//        // 4) sendFile() 메서드에서 내부적으로 OutputStream을 사용하여 파일을 버퍼 8KB로 나눠서 전송(write & flush)한다.
//
//        SplitFileResource splitFileResource = webResourceProvider.getSplitResource(request.getPath());
//        if (!splitFileResource.isEmpty()) {
//            response.sendSplitFile(HttpStatus.OK, splitFileResource);
//        } else {
//            response.sendError(HttpStatus.NOT_FOUND);
//        }
//    }
}
