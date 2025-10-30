package org.dochi.internal.mapper;

import org.dochi.api.handler.HttpApiHandler;
import org.dochi.webserver.attribute.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Map<Path, HttpApiHandler> 타입을 자꾸 수정해야됨 -> 삭제 필요
public class HttpMapper {
    private static final Logger log = LoggerFactory.getLogger(HttpMapper.class);

    private final WebService webService;

    public HttpMapper(WebService webService) {
        this.webService = webService;
    }

    public HttpApiHandler getHttpApiHandler(String path) {
        // HttpApiHandler 객체를 참조하는 객체에서 HttpApiHandler 타입의 객체를 가져와서 메서드를 호출하면 의존성을 띈다.
        // getHttpApiHandler 메서드 내에서 HttpApiHandler 타입의 객체를 가져와서 service 메서드 호출하면 의존성이 제거된다. -> 어댑터 패턴: 서로 호환되지 않는 인터페이스를 연결(bridge)
        HttpApiHandler httpApiHandler = webService.getServices().get(path);
        if (httpApiHandler == null) {
            return webService.getServices().get("/");
        }
        return httpApiHandler;
    }
}
