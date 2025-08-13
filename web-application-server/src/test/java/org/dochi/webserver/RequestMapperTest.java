//package org.dochi.webserver;
//
//import org.dochi.http.api.DefaultHttpApiHandler;
//import org.dochi.http.api.HttpApiHandler;
//import org.dochi.LoginHttpApiHandler;
//import org.dochi.webresource.WebResourceProvider;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//class RequestMapperTest {
//    private HttpApiMapper requestMapper;
//    private final WebResourceProvider webResourceProvider = new WebResourceProvider("webbapp");
//
//    @BeforeEach
//    void setUp() {
//
//        Map<String, HttpApiHandler> requestMappings = Map.of(
//                "/", new DefaultHttpApiHandler(),
//                "/user/create", new LoginHttpApiHandler()
//        );
//        requestMapper = new HttpApiMapper(requestMappings);
//    }
//
//    @Test
//    void get_httpApiHandler_default() {
//        assertEquals(DefaultHttpApiHandler.class, requestMapper.getHttpApiHandler("/").getClass());
//        assertEquals(LoginHttpApiHandler.class, requestMapper.getHttpApiHandler("/user/create").getClass());
//    }
//
//    @Test
//    void get_not_find_httpApiHandler() {
//        assertEquals(DefaultHttpApiHandler.class, requestMapper.getHttpApiHandler("/post").getClass());
//    }
//}