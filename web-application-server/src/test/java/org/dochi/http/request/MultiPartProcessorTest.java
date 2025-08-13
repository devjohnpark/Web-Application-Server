package org.dochi.http.request;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.request.data.Request;
import org.dochi.http.monitor.HttpMessageSizeManager;
import org.dochi.http.request.multipart.MultiPartProcessor;
import org.dochi.http.request.stream.Http11RequestStream;
import org.dochi.webserver.attribute.HttpReqAttribute;
import org.dochi.webserver.config.HttpReqConfig;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class MultiPartProcessorTest {
    Request request = new Request();
    MultiPartProcessor multiPartProcessor;
    ByteArrayInputStream byteArrayInputStream;
    Http11RequestStream http11RequestStream;
    HttpReqConfig httpReqConfig = new HttpReqConfig(new HttpReqAttribute());
    HttpMessageSizeManager httpMessageSizeManager = new HttpMessageSizeManager(httpReqConfig.getRequestHeaderMaxSize(), httpReqConfig.getRequestBodyMaxSize());

    private void createMultipartData(String multipartData) {
        multiPartProcessor = new MultiPartProcessor(httpMessageSizeManager.getBodyMonitor());
        byteArrayInputStream = new ByteArrayInputStream(multipartData.getBytes(StandardCharsets.UTF_8));
        http11RequestStream = new Http11RequestStream(byteArrayInputStream);
    }

    @Test
    void processParts() throws IOException, HttpStatusException {
        String multipartData =
                "--value\r\n" +
                        "Content-Disposition: form-data; name=\"name\"\r\n" +  // name 필드
                        "\r\n" +
                        "John Doe\r\n" +
                        "--value\r\n" +
                        "Content-Disposition: form-data; name=\"age\"\r\n" +  // age 필드
                        "\r\n" +
                        "30\r\n" +
                        "--value\r\n" +
                        "Content-Disposition: form-data; name=\"profileInfo\"\r\n" +  // JSON 데이터 필드
                        "Content-Type: application/json\r\n" +
                        "\r\n" +
                        "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}\r\n" +
                        "--value\r\n" +
                        "Content-Disposition: form-data; name=\"profileImage\"; filename=\"profile.jpg\"\r\n" +  // 파일 필드
                        "Content-Type: image/jpeg\r\n" +
                        "\r\n" +
                        "This is body of multipart/form data\r\n" +
                        "--value--\r\n";

        createMultipartData(multipartData);
        multiPartProcessor.processParts(http11RequestStream, "value", request);
        assertThat(request.multipart().getPart("name").getContent()).isEqualTo("John Doe".getBytes(StandardCharsets.UTF_8));
        assertThat(request.multipart().getPart("age").getContent()).isEqualTo("30".getBytes(StandardCharsets.UTF_8));
        assertThat(request.multipart().getPart("profileInfo").getContent()).isEqualTo("{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}".getBytes(StandardCharsets.UTF_8));
        assertThat(request.multipart().getPart("profileImage").getContent()).isEqualTo("This is body of multipart/form data".getBytes(StandardCharsets.UTF_8));
        request.multipart().recycle();
        assertNull(request.multipart().getPart("age").getContent());
    }

    @Test
    void processParts_include_non_body_part() throws IOException, HttpStatusException {
        String multipartData =
                "--value\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +  // name 필드
                "\r\n" +
                "John Doe\r\n" +
                "--value\r\n" +
                "Content-Disposition: form-data; name=\"age\"\r\n" +  // age 필드
                "\r\n" +
                "30\r\n" +
                "--value\r\n" +
                "Content-Disposition: form-data; name=\"field1\"\r\n" +  // 빈 field1 필드
                "\r\n" +
                "--value\r\n" +
                "Content-Disposition: form-data; name=\"profileInfo\"\r\n" +  // JSON 데이터 필드
                "Content-Type: application/json\r\n" +
                "\r\n" +
                "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}\r\n" +
                "--value\r\n" +
                "Content-Disposition: form-data; name=\"profileImage\"; filename=\"profile.jpg\"\r\n" +  // 파일 필드
                "Content-Type: image/jpeg\r\n" +
                "\r\n" +
                "This is body of multipart/form data\r\n" +
                "--value--\r\n";

        createMultipartData(multipartData);
        multiPartProcessor.processParts(http11RequestStream, "value", request);
        assertThat(request.multipart().getPart("name").getContent()).isEqualTo("John Doe".getBytes(StandardCharsets.UTF_8));
        assertThat(request.multipart().getPart("age").getContent()).isEqualTo("30".getBytes(StandardCharsets.UTF_8));
        assertThat(request.multipart().getPart("field1").getContent()).isEmpty();
        assertThat(request.multipart().getPart("profileInfo").getContent()).isEqualTo("{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}".getBytes(StandardCharsets.UTF_8));
        assertThat(request.multipart().getPart("profileImage").getContent()).isEqualTo("This is body of multipart/form data".getBytes(StandardCharsets.UTF_8));
        request.multipart().recycle();
        assertNull(request.multipart().getPart("age").getContent());
    }

    @Test
    void processParts3() throws IOException, HttpStatusException {
        String multipartData =
                  "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                + "Content-Disposition: form-data; name=\"username\"\r\n"
                + "\r\n"
                + "john\r\n"
                + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                + "Content-Disposition: form-data; name=\"age\"\r\n"
                + "\r\n"
                + "4\r\n"
                + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"imageFile.png\"\r\n"
                + "Content-Type: image/png\r\n"
                + "\r\n"
                + "21312445321553451234213412341234234124234\r\n"
                + "------WebKitFormBoundarylwQGqAAJBIOZfE7B--\r\n";
        createMultipartData(multipartData);
        multiPartProcessor.processParts(http11RequestStream, "----WebKitFormBoundarylwQGqAAJBIOZfE7B", request);
        assertThat(request.multipart().getPart("username").getContent()).isEqualTo("john".getBytes(StandardCharsets.UTF_8));
        assertThat(request.multipart().getPart("age").getContent()).isEqualTo("4".getBytes(StandardCharsets.UTF_8));
        assertThat(request.multipart().getPart("file").getContent()).isEqualTo("21312445321553451234213412341234234124234".getBytes(StandardCharsets.UTF_8));
        request.multipart().recycle();
        assertNull(request.multipart().getPart("age").getContent());
    }
}