package org.dochi.webserver.config;

import org.dochi.api.handler.DefaultHttpApiHandler;
import org.dochi.api.handler.HttpApiHandler;
import org.dochi.webserver.attribute.WebService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebServiceTest {

    private WebService webService;

    @BeforeEach
    void setUp() {
        webService = new WebService();
    }

    @Test
    void constructor() {
        // Given & When
        WebService service = new WebService();
        Map<String, HttpApiHandler> services = service.getServices();

        // Then
        assertNotNull(services);
        assertEquals(1, services.size());
        assertEquals(DefaultHttpApiHandler.class, webService.getServices().get("/").getClass());
    }

    @Test
    void addService() {
        // Given
        String testPath = "/api/test";
        HttpApiHandler testHandler = new DefaultHttpApiHandler();

        // When
        WebService result = webService.addService(testPath, testHandler);

        // Then
        assertSame(webService, result); // 체이닝을 위해 자기 자신을 반환하는지 확인
        Map<String, HttpApiHandler> services = webService.getServices();
        assertEquals(2, services.size()); // 기본 루트 + 추가된 서비스
        assertTrue(services.containsKey(testPath));
        assertSame(testHandler, services.get(testPath));
    }

    @Test
    void addMultipleServices() {
        // Given
        HttpApiHandler handler1 = new DefaultHttpApiHandler();
        HttpApiHandler handler2 = new DefaultHttpApiHandler();
        HttpApiHandler handler3 = new DefaultHttpApiHandler();

        // When
        webService
                .addService("/api/users", handler1)
                .addService("/api/products", handler2)
                .addService("/api/orders", handler3);

        // Then
        Map<String, HttpApiHandler> services = webService.getServices();
        assertEquals(4, services.size()); // 기본 루트 + 3개 추가
        assertTrue(services.containsKey("/api/users"));
        assertTrue(services.containsKey("/api/products"));
        assertTrue(services.containsKey("/api/orders"));
        assertSame(handler1, services.get("/api/users"));
        assertSame(handler2, services.get("/api/products"));
        assertSame(handler3, services.get("/api/orders"));
    }

    @Test
    void addServiceWithSamePath() {
        // Given
        HttpApiHandler firstHandler = new DefaultHttpApiHandler();
        HttpApiHandler secondHandler = new DefaultHttpApiHandler();
        String testPath = "/api/test";

        // When
        webService.addService(testPath, firstHandler);
        webService.addService(testPath, secondHandler);

        // Then
        Map<String, HttpApiHandler> services = webService.getServices();
        assertEquals(2, services.size()); // 루트 + 하나의 테스트 경로
        assertSame(secondHandler, services.get(testPath)); // 두 번째 핸들러로 덮어써짐
    }

    @Test
    void getServices() {
        // Given
        HttpApiHandler testHandler = new DefaultHttpApiHandler();
        webService.addService("/test", testHandler);

        // When
        Map<String, HttpApiHandler> services = webService.getServices();

        // Then
        assertNotNull(services);
        assertEquals(2, services.size());
        assertTrue(services.containsKey("/"));
        assertTrue(services.containsKey("/test"));
    }

    @Test
    void getServiceConfigSingleton() {
        // When
        WebServiceConfig config1 = webService.getServiceConfig();
        WebServiceConfig config2 = webService.getServiceConfig();

        // Then
        assertNotNull(config1);
        assertNotNull(config2);
        assertSame(config1, config2); // 동일한 인스턴스여야 함
    }

    @Test
    void addServiceWithNullHandler() {
        // Given
        String path = "/test";

        // When
        webService.addService(path, null);

        // Then
        Map<String, HttpApiHandler> services = webService.getServices();
        assertTrue(services.containsKey(path));
        assertNull(services.get(path));
    }

    @Test
    void addServiceWithEmptyPath() {
        // Given
        String emptyPath = "";
        HttpApiHandler handler = new DefaultHttpApiHandler();

        // When
        webService.addService(emptyPath, handler);

        // Then
        Map<String, HttpApiHandler> services = webService.getServices();
        assertTrue(services.containsKey(emptyPath));
        assertSame(handler, services.get(emptyPath));
    }
}