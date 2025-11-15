package org.dochi.webserver.lifecycle;

import org.dochi.api.handler.DefaultHttpApiHandler;
import org.dochi.api.handler.HttpApiHandler;
import org.dochi.webserver.bootstrap.WebService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        // Then
        assertEquals(1, service.getSize());
        assertEquals(DefaultHttpApiHandler.class, webService.getService("/").getClass());
    }

    @Test
    void addService() {
        // Given
        String testPath = "/api/test";
        HttpApiHandler testHandler = new DefaultHttpApiHandler();

        // When
        WebService result = webService.addService(testPath, testHandler);

        // Then
        assertSame(webService, result);
        assertEquals(2, webService.getSize());
        assertSame(testHandler, webService.getService(testPath));
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
        assertEquals(4, webService.getSize()); // 기본 루트 + 3개 추가
        assertSame(handler1, webService.getService("/api/users"));
        assertSame(handler2, webService.getService("/api/products"));
        assertSame(handler3, webService.getService("/api/orders"));
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
        assertEquals(2, webService.getSize()); // 루트 + 하나의 테스트 경로
        assertSame(secondHandler, webService.getService(testPath)); // 두 번째 핸들러로 덮어써짐
    }

    @Test
    void getServices() {
        HttpApiHandler testHandler = new DefaultHttpApiHandler();
        webService.addService("/test", testHandler);

        assertEquals(2, webService.getSize());
        assertInstanceOf(testHandler.getClass(), webService.getService("/"));
        assertEquals(testHandler, webService.getService("/test"));
    }


    @Test
    void addServiceWithNullHandler() {
        // Given
        String path = "/test";

        // When
        webService.addService(path, null);

        // Then
        assertNull(webService.getService(path));
    }
}