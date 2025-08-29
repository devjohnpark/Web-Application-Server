package org.dochi.http.multipart;

import org.dochi.webresource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

class PartTest {

    private Part part;
    private byte[] testContent;
    private String testContentType;
    private String testFileName;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testContent = "test content".getBytes();
        testContentType = "text/plain";
        testFileName = "test.txt";
        testUuid = UUID.randomUUID();
    }

    // 기본 생성자 테스트
    @Test
    void createEmptyPartWithDefaultConstructor() {
        part = new Part();

        assertNull(part.getFileName());
        assertNull(part.getContentType());
        assertFalse(part.isFile());
        assertTrue(part.isEmpty());
    }

    @Test
    void shouldReturnNullContentWithDefaultConstructor() throws IOException {
        part = new Part();
        assertNull(part.getContent());
    }

    // Part(byte[] content, String contentType) 생성자 테스트
    @Test
    void createPartWithContentAndType() {
        part = new Part(testContent, testContentType);

        assertNull(part.getFileName());
        assertEquals(testContentType, part.getContentType());
        assertFalse(part.isFile());
        assertFalse(part.isEmpty());
    }

    @Test
    void returnContentDirectlyForNonFilePart() throws IOException {
        part = new Part(testContent, testContentType);
        assertArrayEquals(testContent, part.getContent());
    }

    @Test
    void createPartWithNullContent() {
        part = new Part(null, testContentType);

        assertEquals(testContentType, part.getContentType());
        assertTrue(part.isEmpty());
    }

    @Test
    void createPartWithNullContentType() {
        part = new Part(testContent, null);

        assertEquals("text/plain; charset=UTF-8", part.getContentType());
    }

    @Test
    void createPartWithBothNullValues() {
        part = new Part(null, null);

        assertNull(part.getContentType());
        assertTrue(part.isEmpty());
    }

    // Part(byte[] content, String contentType, String fileName, UUID uuid) 생성자 테스트
    @Test
    void createFilePartSuccessfully() throws IOException {
        part = new Part(testContent, testContentType, testFileName, testUuid);

        assertEquals(testFileName, part.getFileName());
        assertEquals(testContentType, part.getContentType());
        assertTrue(part.isFile());
        assertTrue(part.isEmpty()); // content는 파일로 저장되므로 null
    }

    @Test
    void shouldReturnFileNameWhenSet() throws IOException {
        part = new Part(testContent, testContentType, testFileName, testUuid);
        assertEquals(testFileName, part.getFileName());
    }

    @Test
    void shouldReturnNullFileNameForNonFilePart() {
        part = new Part(testContent, testContentType);
        assertNull(part.getFileName());
    }

    @Test
    void shouldReturnContentTypeWhenSet() {
        part = new Part(testContent, testContentType);
        assertEquals(testContentType, part.getContentType());
    }

    @Test
    void shouldReturnDefaultContentTypeWhenContentExistsButTypeIsNull() {
        part = new Part(testContent, null);
        assertEquals(ResourceType.TEXT.getContentType("UTF-8"), part.getContentType());
    }

    @Test
    void shouldReturnNullContentTypeWhenContentIsNull() {
        part = new Part(null, null);
        assertNull(part.getContentType());
    }

    @Test
    void shouldReturnNullContentTypeWhenContentIsEmpty() {
        part = new Part(new byte[0], null);
        assertNull(part.getContentType());
    }

    @Test
    void shouldReturnContentFromFileManagerForFilePart() throws IOException {
        part = new Part(testContent, testContentType, testFileName, testUuid);

        // MultipartFileManager에서 파일을 다운로드하여 반환
        byte[] retrievedContent = part.getContent();
        assertNotNull(retrievedContent);
        // 실제 파일 관리자의 구현에 따라 내용이 일치할 것으로 예상
        assertArrayEquals(testContent, retrievedContent);
    }

    @Test
    void returnDirectContentForNonFilePart() throws IOException {
        part = new Part(testContent, testContentType);
        assertArrayEquals(testContent, part.getContent());
    }

    @Test
    void returnTrueWhenContentIsNull() {
        part = new Part(null, testContentType);
        assertTrue(part.isEmpty());
    }

    @Test
    void returnFalseWhenContentExists() {
        part = new Part(testContent, testContentType);
        assertFalse(part.isEmpty());
    }

    @Test
    void returnFalseWhenContentIsEmptyArray() {
        part = new Part(new byte[0], testContentType);
        assertFalse(part.isEmpty());
    }

    @Test
    void returnTrueForDefaultConstructor() {
        part = new Part();
        assertTrue(part.isEmpty());
    }

    @Test
    void returnTrueForFilePartSinceContentIsNotStored() throws IOException {
        part = new Part(testContent, testContentType, testFileName, testUuid);
        assertTrue(part.isEmpty()); // 파일 Part는 content를 직접 저장하지 않음
    }

    @Test
    void handleCompleteFilePartLifecycle() throws IOException {
        part = new Part(testContent, testContentType, testFileName, testUuid);

        assertTrue(part.isFile());
        assertEquals(testFileName, part.getFileName());
        assertEquals(testContentType, part.getContentType());
        assertTrue(part.isEmpty()); // content는 파일로 저장됨

        assertNotNull(part.getContent());

        part.removeFile();

        assertThrows(FileNotFoundException.class, () -> part.getContent());

        assertTrue(part.isEmpty());
    }

    @Test
    void handleNonFilePartCorrectly() throws IOException {
        part = new Part(testContent, testContentType);

        assertFalse(part.isFile());
        assertNull(part.getFileName());
        assertEquals(testContentType, part.getContentType());
        assertFalse(part.isEmpty());
        assertArrayEquals(testContent, part.getContent());

        // 파일이 아니므로 removeFile은 아무것도 하지 않아서 예외 발생하지 않음
        assertDoesNotThrow(() -> part.removeFile());
    }
}
