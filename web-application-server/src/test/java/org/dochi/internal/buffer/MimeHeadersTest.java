package org.dochi.internal.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MimeHeadersTest {

    private MimeHeaders mimeHeaders;

    @BeforeEach
    void setUp() {
        mimeHeaders = new MimeHeaders();
    }

    @Test
    void defaultConstructorInitializesWithDefaultSize() {
        MimeHeaders headers = new MimeHeaders();
        assertEquals(0, headers.size());
    }

    @Test
    void constructorWithCustomSizeInitializesCorrectly() {
        MimeHeaders headers = new MimeHeaders(16);
        assertEquals(0, headers.size());
    }

    @Test
    void createHeaderIncreasesSize() {
        MimeHeaderField header = mimeHeaders.createHeader();
        assertNotNull(header);
        assertEquals(1, mimeHeaders.size());

        MimeHeaderField header2 = mimeHeaders.createHeader();
        assertNotNull(header2);
        assertEquals(2, mimeHeaders.size());
    }

    @Test
    void createHeaderExpandsArrayWhenNeeded() {
        // 기본 크기는 8이므로 9개 생성해서 배열 확장 테스트
        for (int i = 0; i < 9; i++) {
            mimeHeaders.createHeader();
        }
        assertEquals(9, mimeHeaders.size());
    }

    @Test
    void createHeaderWithLargeNumberOfHeaders() {
        // 100개의 헤더 생성으로 배열 확장 여러 번 테스트
        for (int i = 0; i < 100; i++) {
            mimeHeaders.createHeader();
        }
        assertEquals(100, mimeHeaders.size());
    }

    @Test
    void recycleResetsCountToZero() {
        mimeHeaders.createHeader();
        mimeHeaders.createHeader();
        assertEquals(2, mimeHeaders.size());

        mimeHeaders.recycle();
        assertEquals(0, mimeHeaders.size());
    }

    @Test
    void getValueByNameReturnsCorrectValue() {
        MimeHeaderField header1 = mimeHeaders.createHeader();
        header1.getName().setString("Content-Type");
        header1.getValue().setString("application/json");

        MimeHeaderField header2 = mimeHeaders.createHeader();
        header2.getName().setString("Accept");
        header2.getValue().setString("text/html");

        MessageBytes value = mimeHeaders.getValue("Content-Type");
        assertNotNull(value);
        assertEquals("application/json", value.toString());

        MessageBytes value2 = mimeHeaders.getValue("Accept");
        assertNotNull(value2);
        assertEquals("text/html", value2.toString());
    }

    @Test
    void getValueByNameIsCaseInsensitive() {
        MimeHeaderField header = mimeHeaders.createHeader();
        header.getName().setString("Content-Type");
        header.getValue().setString("application/json");

        MessageBytes value1 = mimeHeaders.getValue("content-type");
        MessageBytes value2 = mimeHeaders.getValue("CONTENT-TYPE");
        MessageBytes value3 = mimeHeaders.getValue("Content-Type");

        assertNotNull(value1);
        assertNotNull(value2);
        assertNotNull(value3);
        assertEquals("application/json", value1.toString());
        assertEquals("application/json", value2.toString());
        assertEquals("application/json", value3.toString());
    }

    @Test
    void getValueByNameReturnsNullForNonExistentHeader() {
        MimeHeaderField header = mimeHeaders.createHeader();
        header.getName().setString("Content-Type");
        header.getValue().setString("application/json");

        MessageBytes value = mimeHeaders.getValue("Accept");
        assertNull(value);
    }

    @Test
    void getValueByNameReturnsNullForEmptyHeaders() {
        MessageBytes value = mimeHeaders.getValue("Content-Type");
        assertNull(value);
    }

    @Test
    void getValueByNameHandlesNullParameter() {
        MimeHeaderField header = mimeHeaders.createHeader();
        header.getName().setString("Content-Type");
        header.getValue().setString("application/json");

        MessageBytes value = mimeHeaders.getValue(null);
        assertNull(value);
    }

    @Test
    void getHeaderReturnsStringValue() {
        MimeHeaderField header = mimeHeaders.createHeader();
        header.getName().setString("Content-Type");
        header.getValue().setString("application/json");

        String headerValue = mimeHeaders.getHeader("Content-Type");
        assertEquals("application/json", headerValue);
    }

    @Test
    void getHeaderReturnsNullForNonExistentHeader() {
        String headerValue = mimeHeaders.getHeader("Non-Existent");
        assertNull(headerValue);
    }

    @Test
    void getHeaderIsCaseInsensitive() {
        MimeHeaderField header = mimeHeaders.createHeader();
        header.getName().setString("Content-Type");
        header.getValue().setString("application/json");

        String value1 = mimeHeaders.getHeader("content-type");
        String value2 = mimeHeaders.getHeader("CONTENT-TYPE");
        String value3 = mimeHeaders.getHeader("Content-Type");

        assertEquals("application/json", value1);
        assertEquals("application/json", value2);
        assertEquals("application/json", value3);
    }

    @Test
    void getHeaderHandlesNullParameter() {
        MimeHeaderField header = mimeHeaders.createHeader();
        header.getName().setString("Content-Type");
        header.getValue().setString("application/json");

        String headerValue = mimeHeaders.getHeader(null);
        assertNull(headerValue);
    }

    @Test
    void multipleHeadersWithSameNameReturnsFirst() {
        MimeHeaderField header1 = mimeHeaders.createHeader();
        header1.getName().setString("Accept");
        header1.getValue().setString("text/html");

        MimeHeaderField header2 = mimeHeaders.createHeader();
        header2.getName().setString("Accept");
        header2.getValue().setString("application/json");

        MessageBytes value = mimeHeaders.getValue("Accept");
        assertNotNull(value);
        assertEquals("text/html", value.toString());

        String headerValue = mimeHeaders.getHeader("Accept");
        assertEquals("text/html", headerValue);
    }

    @Test
    void recycleAfterMultipleOperations() {
        // 여러 헤더 생성
        for (int i = 0; i < 5; i++) {
            MimeHeaderField header = mimeHeaders.createHeader();
            header.getName().setString("Header" + i);
            header.getValue().setString("Value" + i);
        }

        assertEquals(5, mimeHeaders.size());

        // 리사이클 후 크기 확인
        mimeHeaders.recycle();
        assertEquals(0, mimeHeaders.size());

        // 리사이클 후 다시 헤더 생성 가능한지 확인
        MimeHeaderField newHeader = mimeHeaders.createHeader();
        newHeader.getName().setString("NewHeader");
        newHeader.getValue().setString("NewValue");

        assertEquals(1, mimeHeaders.size());
        assertEquals("NewValue", mimeHeaders.getHeader("NewHeader"));
    }

    @Test
    void createHeaderReturnsUniqueInstances() {
        MimeHeaderField header1 = mimeHeaders.createHeader();
        MimeHeaderField header2 = mimeHeaders.createHeader();

        assertNotSame(header1, header2);
        assertNotNull(header1);
        assertNotNull(header2);
    }

    @Test
    void arrayExpansionPreservesExistingHeaders() {
        // 기본 크기(8)만큼 헤더 생성
        for (int i = 0; i < 8; i++) {
            MimeHeaderField header = mimeHeaders.createHeader();
            header.getName().setString("Header" + i);
            header.getValue().setString("Value" + i);
        }

        // 첫 번째 헤더 확인
        assertEquals("Value0", mimeHeaders.getHeader("Header0"));

        // 배열 확장을 유발하는 9번째 헤더 생성
        MimeHeaderField header9 = mimeHeaders.createHeader();
        header9.getName().setString("Header8");
        header9.getValue().setString("Value8");

        // 기존 헤더들이 여전히 유효한지 확인
        assertEquals("Value0", mimeHeaders.getHeader("Header0"));
        assertEquals("Value7", mimeHeaders.getHeader("Header7"));
        assertEquals("Value8", mimeHeaders.getHeader("Header8"));
        assertEquals(9, mimeHeaders.size());
    }

    @Test
    void emptyHeaderNameAndValueHandling() {
        MimeHeaderField header = mimeHeaders.createHeader();
        header.getName().setString("");
        header.getValue().setString("");

        MessageBytes value = mimeHeaders.getValue("");
        assertNotNull(value);
        assertEquals("", value.toString());

        String headerValue = mimeHeaders.getHeader("");
        assertEquals("", headerValue);
    }

    @Test
    void headerWithNullValue() {
        MimeHeaderField header = mimeHeaders.createHeader();
        header.getName().setString("Test-Header");
        // getValue()는 기본적으로 빈 MessageBytes를 반환하므로 null 값 설정 테스트

        MessageBytes value = mimeHeaders.getValue("Test-Header");
        assertNotNull(value);

        String headerValue = mimeHeaders.getHeader("Test-Header");
        // MessageBytes의 toString() 구현에 따라 결과가 달라질 수 있음
        assertNotNull(headerValue);
    }
}