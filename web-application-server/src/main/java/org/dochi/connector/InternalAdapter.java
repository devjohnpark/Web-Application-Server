package org.dochi.connector;

import org.dochi.api.handler.HttpApiHandler;
import org.dochi.webserver.attribute.HttpReqAttribute;
import org.dochi.webserver.attribute.HttpResAttribute;
import org.dochi.webserver.attribute.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class InternalAdapter implements Adapter {
    private static final Logger log = LoggerFactory.getLogger(InternalAdapter.class);

    private final Connector connector;

    // HttpDispatcher를 HttpAdapter로 변경 (service 메서드 정의한 Adapter 인터페이스 정의)
    // WebService: 컨테이너와 요청/응답 설정 정보를 포함한 객체로 변경
    // Connector: WebService 컨테이너와 요청/응답 설정 정보를 포함한 connector 계층의 객체

    public InternalAdapter(Connector connector) {
        this.connector = connector;
    }

    // HttpApiHandler 타입 의존성
    // 문제
    // Request Path에 부합하는 HttpApiHandler를 찾아서 반환하고, 반환받은 객체에서 service()를 호출
    // HttpApiHandler 타입 때문에 의존성을 띄어서 HttpApiHandler 외에 다른 인터페이스로 변경 불가능
    // 해결
    // 해당 객체에서 HttpApiHandler를 가져와 HTTP API service를 실행하도록한다.
    // 호출할 HttpApiHandler의 service() 메서드명과 일치시키기 위해 Adapter 패턴 사용

    // 저수준과 고수준의 계층 통합
    // 문제
    // 저수준과 고수준 계층중에서 어느 계층에서 문제가 발생하는지 파악하기가 어렵다.
    // 객체의 책임 커져서 코드가 복잡하고 기능이 방대해진다.
    // 해결
    // Adpater에서 저수준의 요청/응답 객체를 고수준 객체로 adapt 한다.

    // 구현 문제
    // Low-High Level을 잇는 역할의 Adapter는 각 서버 인스턴스 별로 하나 존재
    // internal 객체와 external 객체은 1대1로 매칭되어야함
    // 방법 1. Adapter에서 internal 객체와 매핑되는 external 객체 Pool 가지고 있음
    //  * 장점: 완전한 계층 분리
    //  * 단점: 동시성 로직으로 인한 성능 저하 (WAS 부적합)
    // 방법 2. internal 객체애 external 객체를 매핑
    //  * 장점: 간단한 1:1 매칭 구조
    //  * 단점: low-level 객체가 high level 객체를 역참조
    //    * internal 객체에서 외관(facade)으로 사용하는 단 하나의 객체 생성 허용
    //    * 이 객체는 WAS 에서 외부로 노출되는 HTTP API 에 제공하므로 의미 부합
    //    * 이로써 internal 객체는 자기 자신을 감싸서 facade 역할을 하는 객체 생성과 가져올수있도록 get/set 메서드 제공
    // internal 요청/응답 컨텍스트 처리를 위한 객체
    //  * Request/Response Context 인터페이스
    //  * Request/Response Facade 추상 클래스
    //  * External Request/Response 인터페이스로

    public void service(org.dochi.internal.request.Request req, org.dochi.internal.response.Response res) throws IOException {
        Request request = ensureRequestFacade(req);
        Response response = ensureResponseFacade(res);

        HttpApiHandler httpApiHandler = connector.getServices().get(request.getPath());
        if (httpApiHandler == null) {
            httpApiHandler = connector.getServices().get("/");
        }
        httpApiHandler.service(request, response);
        recycle(request, response);
    }

    private void recycle(Request request, Response response) {
        request.recycle();
        response.recycle();
    }

    private Request ensureRequestFacade(org.dochi.internal.request.Request internalRequest) {
        Request externalRequest = (Request) internalRequest.getFacade();
        if (externalRequest == null) {
            externalRequest = new Request(internalRequest, connector.getHttpReqConfig());
            internalRequest.setFacade(externalRequest);
        }
        return externalRequest;
    }

    private Response ensureResponseFacade(org.dochi.internal.response.Response internalResponse) {
        Response externalResponse = (Response) internalResponse.getFacade();
        if (externalResponse == null) {
            externalResponse = new Response(internalResponse, connector.getHttpResConfig());
            internalResponse.setFacade(externalResponse);
        }
        return externalResponse;
    }
}
