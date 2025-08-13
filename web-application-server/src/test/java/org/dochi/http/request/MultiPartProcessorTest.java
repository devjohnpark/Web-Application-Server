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

//        http11RequestStream = new Http11RequestStream(byteArrayInputStream,
//            httpReqConfig.getRequestHeaderMaxSize(),
//            httpReqConfig.getRequestBodyMaxSize(),
//            new HttpInputSizeListener() {
//                @Override
//                public void onHeaderRead(int size) throws HttpStatusException {
//                    httpMessageSizeManager.addHeaderSize(size);
//                }
//
//                @Override
//                public void onBodyRead(int size) throws HttpStatusException {
//                    httpMessageSizeManager.addBodySize(size);
//                }
//        });
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
        assertEquals(httpMessageSizeManager.getContentMonitor().getActualContentLength(), multipartData.getBytes(StandardCharsets.UTF_8).length);
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
        assertEquals(httpMessageSizeManager.getContentMonitor().getActualContentLength(), multipartData.getBytes(StandardCharsets.UTF_8).length);
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
        assertEquals(httpMessageSizeManager.getContentMonitor().getActualContentLength(), multipartData.getBytes(StandardCharsets.UTF_8).length);
    }

//
//    @Test
//    void readBody_end() throws IOException {
//        MultiPartProcessor multiPartProcessor = new MultiPartProcessor();
//        String file = "\\xFF\\xD8\\xFF\\xE0\\x00\\x10\\x4A\\x46\\x49\\x46\\x00\\x01\\x01\\x01\\x00\\x60\\x00\\x60\\x00\\x00 \\xFF\\xDB\\x00\\x43\\x00\\x08\\x06\\x06\\x07\\x06\\x05\\x08\\x07\\x07\\x07\\x09\\x09\\x08\\x0A\\x0C \\x14\\x0D\\x0C\\x0B\\x0B\\x0C\\x19\\x12\\x13\\x0F\\x14\\x1D\\x1A\\x1F\\x1E\\x1D\\x1A\\x1C\\x1C\\x20 \\x24\\x2E\\x27\\x20\\x22\\x2C\\x23\\x1C\\x1C\\x28\\x37\\x29\\x2C\\x30\\x31\\x34\\x34\\x34\\x1F\\x27 \\x39\\x3D\\x38\\x32\\x3C\\x2E\\x33\\x34\\x32";
//        String boundaryValue = "12345";
//        multiPartProcessor.setBoundaryValue(boundaryValue);
//        String body = file + "\r\n" + "--" + boundaryValue + "--" + "\r\n";
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
//        Http11RequestStream http11RequestStream = new Http11RequestStream(byteArrayInputStream);
//        assertArrayEquals(file.getBytes(StandardCharsets.UTF_8), multiPartProcessor.readBody(http11RequestStream));
//    }
//
//    @Test
//    void readBody_last_part() throws IOException {
//        MultiPartProcessor multiPartProcessor = new MultiPartProcessor();
//        byte[] binaryData = {0x10, 0x20, 0x30, 0x40, 0x50};
//        String boundaryValue = "12345";
//        multiPartProcessor.setBoundaryValue(boundaryValue);
//
//        String boundaryString = "\r\n" + "--" + boundaryValue + "--" + "\r\n";
//        byte[] boundaryBytes = boundaryString.getBytes(StandardCharsets.UTF_8);
//
//        String testDir = "./src/test/resources/";
//        String filePath = "multipart_last_part_body.bin";
//
//        try (
//                FileOutputStream fos = new FileOutputStream(testDir + filePath);
//                FileInputStream fis = new FileInputStream(testDir + filePath);
//        ) {
//            // 기존 바이너리 데이터 쓰기
//            fos.write(binaryData);
//
//            // Boundary 데이터 쓰기
//            fos.write(boundaryBytes);
//
//            Http11RequestStream http11RequestStream = new Http11RequestStream(fis);
//
//            assertArrayEquals(binaryData, multiPartProcessor.readBody(http11RequestStream));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    @Test
//    void readBody_not_last_part() throws IOException {
//        MultiPartProcessor multiPartProcessor = new MultiPartProcessor();
//        byte[] binaryData = {0x10, 0x20, 0x30, 0x40, 0x50};
//        String boundaryValue = "12345";
//        multiPartProcessor.setBoundaryValue(boundaryValue);
//
//        String boundaryString = "\r\n" + "--" + boundaryValue + "\r\n";
//        byte[] boundaryBytes = boundaryString.getBytes(StandardCharsets.UTF_8);
//
//        String testDir = "./src/test/resources/";
//        String filePath = "multipart_last_part_body.bin";
//
//        try (
//                FileOutputStream fos = new FileOutputStream(testDir + filePath);
//                FileInputStream fis = new FileInputStream(testDir + filePath);
//        ) {
//            // 기존 바이너리 데이터 쓰기
//            fos.write(binaryData);
//
//            // Boundary 데이터 쓰기
//            fos.write(boundaryBytes);
//
//            Http11RequestStream http11RequestStream = new Http11RequestStream(fis);
//
//            assertArrayEquals(binaryData, multiPartProcessor.readBody(http11RequestStream));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    void readBody_not_found_boundary_wrong_crlf() throws IOException {
//        MultiPartProcessor multiPartProcessor = new MultiPartProcessor();
//        byte[] binaryData = {0x10, 0x20, 0x30, 0x40, 0x50};
//        String boundaryValue = "12345";
//        multiPartProcessor.setBoundaryValue(boundaryValue);
//
//        String boundaryString = "\r" + "--" + boundaryValue + "\r\n";
//        byte[] boundaryBytes = boundaryString.getBytes(StandardCharsets.UTF_8);
//
//        String testDir = "./src/test/resources/";
//        String filePath = "multipart_last_part_body.bin";
//
//        try (
//                FileOutputStream fos = new FileOutputStream(testDir + filePath);
//                FileInputStream fis = new FileInputStream(testDir + filePath);
//        ) {
//            // 기존 바이너리 데이터 쓰기
//            fos.write(binaryData);
//
//            // Boundary 데이터 쓰기
//            fos.write(boundaryBytes);
//
//            Http11RequestStream http11RequestStream = new Http11RequestStream(fis);
//
//            assertThrows(IOException.class, () -> multiPartProcessor.readBody(http11RequestStream));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    void readBody_not_found_boundary_wrong_boundaryValue() throws IOException {
//        MultiPartProcessor multiPartProcessor = new MultiPartProcessor();
//        byte[] binaryData = {0x10, 0x20, 0x30, 0x40, 0x50};
//        String boundaryValue = "12345";
//        multiPartProcessor.setBoundaryValue(boundaryValue);
//
//        String boundaryString = "\r\n" + "--" + "123456" + "\r\n";
//        byte[] boundaryBytes = boundaryString.getBytes(StandardCharsets.UTF_8);
//
//        String testDir = "./src/test/resources/";
//        String filePath = "multipart_last_part_body.bin";
//
//        try (
//                FileOutputStream fos = new FileOutputStream(testDir + filePath);
//                FileInputStream fis = new FileInputStream(testDir + filePath);
//        ) {
//            // 기존 바이너리 데이터 쓰기
//            fos.write(binaryData);
//
//            // Boundary 데이터 쓰기
//            fos.write(boundaryBytes);
//
//            Http11RequestStream http11RequestStream = new Http11RequestStream(fis);
//
//            assertThrows(IOException.class, () -> multiPartProcessor.readBody(http11RequestStream));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}