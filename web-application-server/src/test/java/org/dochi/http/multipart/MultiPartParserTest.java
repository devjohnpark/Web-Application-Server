package org.dochi.http.multipart;

import org.dochi.http.exception.HttpStatusException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MultiPartParserTest {
    MultiPartParser multiPartParser;
    Multipart multipart = new Multipart();

    @AfterEach
    void recycle() {
        multipart.recycle();
    }

    private void createMultipartData(String multipartData) {
        multiPartParser = new MultiPartParser(new MultipartStream(new ByteArrayInputStream(multipartData.getBytes(StandardCharsets.UTF_8))), 1024, 8192);
    }

    @Test
    void parseParts() throws IOException {
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
        multiPartParser.parseParts("value", multipart);
        assertThat(multipart.getPart("name").getContent()).isEqualTo("John Doe".getBytes(StandardCharsets.UTF_8));
        assertThat(multipart.getPart("age").getContent()).isEqualTo("30".getBytes(StandardCharsets.UTF_8));
        assertThat(multipart.getPart("profileInfo").getContent()).isEqualTo("{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}".getBytes(StandardCharsets.UTF_8));
        assertThat(multipart.getPart("profileImage").getContent()).isEqualTo("This is body of multipart/form data".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parseParts_include_non_body_part() throws IOException, HttpStatusException {
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
        multiPartParser.parseParts("value", multipart);
        assertThat(multipart.getPart("name").getContent()).isEqualTo("John Doe".getBytes(StandardCharsets.UTF_8));
        assertThat(multipart.getPart("age").getContent()).isEqualTo("30".getBytes(StandardCharsets.UTF_8));
        assertThat(multipart.getPart("field1").getContent()).isEmpty();
        assertThat(multipart.getPart("profileInfo").getContent()).isEqualTo("{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}".getBytes(StandardCharsets.UTF_8));
        assertThat(multipart.getPart("profileImage").getContent()).isEqualTo("This is body of multipart/form data".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parseParts3() throws IOException, HttpStatusException {
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
        multiPartParser.parseParts("----WebKitFormBoundarylwQGqAAJBIOZfE7B", multipart);
        assertThat(multipart.getPart("username").getContent()).isEqualTo("john".getBytes(StandardCharsets.UTF_8));
        assertThat(multipart.getPart("age").getContent()).isEqualTo("4".getBytes(StandardCharsets.UTF_8));
        assertThat(multipart.getPart("file").getContent()).isEqualTo("21312445321553451234213412341234234124234".getBytes(StandardCharsets.UTF_8));
    }
}