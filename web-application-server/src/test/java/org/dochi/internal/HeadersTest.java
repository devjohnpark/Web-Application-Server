package org.dochi.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeadersTest {

    private Headers headers;

    @BeforeEach
    void setUp() {
        headers = new Headers();
    }

    @Test
    void defaultConstructorInitializesWithDefaultSize() {
        Headers headers = new Headers();
        assertEquals(0, headers.size());
    }

    @Test
    void constructorWithCustomSizeInitializesCorrectly() {
        Headers headers = new Headers(16);
        assertEquals(0, headers.size());
    }

    @Test
    void createHeaderIncreasesSize() {
        HeaderField header = headers.createHeader();
        assertNotNull(header);
        assertEquals(1, headers.size());

        HeaderField header2 = headers.createHeader();
        assertNotNull(header2);
        assertEquals(2, headers.size());
    }

    @Test
    void createHeaderExpandsArrayWhenNeeded() {
        // 기본 크기는 8이므로 9개 생성해서 배열 확장 테스트
        for (int i = 0; i < 9; i++) {
            headers.createHeader();
        }
        assertEquals(9, headers.size());
    }

    @Test
    void createHeaderWithLargeNumberOfHeaders() {
        // 100개의 헤더 생성으로 배열 확장 여러 번 테스트
        for (int i = 0; i < 100; i++) {
            headers.createHeader();
        }
        assertEquals(100, headers.size());
    }

    @Test
    void recycleResetsCountToZero() {
        headers.createHeader();
        headers.createHeader();
        assertEquals(2, headers.size());

        headers.recycle();
        assertEquals(0, headers.size());
    }

    @Test
    void getValueByNameReturnsCorrectValue() {
        HeaderField header1 = headers.createHeader();
        header1.name().setString("Content-Type");
        header1.getValue().setString("application/json");

        HeaderField header2 = headers.createHeader();
        header2.name().setString("Accept");
        header2.getValue().setString("text/html");

        HeaderBytes value = headers.getValue("Content-Type");
        assertNotNull(value);
        assertEquals("application/json", value.toString());

        HeaderBytes value2 = headers.getValue("Accept");
        assertNotNull(value2);
        assertEquals("text/html", value2.toString());
    }

    @Test
    void getValueByNameIsCaseInsensitive() {
        HeaderField header = headers.createHeader();
        header.name().setString("Content-Type");
        header.getValue().setString("application/json");

        HeaderBytes value1 = headers.getValue("content-type");
        HeaderBytes value2 = headers.getValue("CONTENT-TYPE");
        HeaderBytes value3 = headers.getValue("Content-Type");

        assertNotNull(value1);
        assertNotNull(value2);
        assertNotNull(value3);
        assertEquals("application/json", value1.toString());
        assertEquals("application/json", value2.toString());
        assertEquals("application/json", value3.toString());
    }

    @Test
    void getValueByNameReturnsNullForNonExistentHeader() {
        HeaderField header = headers.createHeader();
        header.name().setString("Content-Type");
        header.getValue().setString("application/json");

        HeaderBytes value = headers.getValue("Accept");
        assertNull(value);
    }

    @Test
    void getValueByNameReturnsNullForEmptyHeaders() {
        HeaderBytes value = headers.getValue("Content-Type");
        assertNull(value);
    }

    @Test
    void getValueByNameHandlesNullParameter() {
        HeaderField header = headers.createHeader();
        header.name().setString("Content-Type");
        header.getValue().setString("application/json");

        HeaderBytes value = headers.getValue(null);
        assertNull(value);
    }

    @Test
    void getHeaderReturnsStringValue() {
        HeaderField header = headers.createHeader();
        header.name().setString("Content-Type");
        header.getValue().setString("application/json");

        String headerValue = headers.getHeader("Content-Type");
        assertEquals("application/json", headerValue);
    }

    @Test
    void getHeaderReturnsNullForNonExistentHeader() {
        String headerValue = headers.getHeader("Non-Existent");
        assertNull(headerValue);
    }

    @Test
    void getHeaderIsCaseInsensitive() {
        HeaderField header = headers.createHeader();
        header.name().setString("Content-Type");
        header.getValue().setString("application/json");

        String value1 = headers.getHeader("content-type");
        String value2 = headers.getHeader("CONTENT-TYPE");
        String value3 = headers.getHeader("Content-Type");

        assertEquals("application/json", value1);
        assertEquals("application/json", value2);
        assertEquals("application/json", value3);
    }

    @Test
    void getHeaderHandlesNullParameter() {
        HeaderField header = headers.createHeader();
        header.name().setString("Content-Type");
        header.getValue().setString("application/json");

        String headerValue = headers.getHeader(null);
        assertNull(headerValue);
    }

    @Test
    void multipleHeadersWithSameNameReturnsFirst() {
        HeaderField header1 = headers.createHeader();
        header1.name().setString("Accept");
        header1.getValue().setString("text/html");

        HeaderField header2 = headers.createHeader();
        header2.name().setString("Accept");
        header2.getValue().setString("application/json");

        HeaderBytes value = headers.getValue("Accept");
        assertNotNull(value);
        assertEquals("text/html", value.toString());

        String headerValue = headers.getHeader("Accept");
        assertEquals("text/html", headerValue);
    }

    @Test
    void recycleAfterMultipleOperations() {
        // 여러 헤더 생성
        for (int i = 0; i < 5; i++) {
            HeaderField header = headers.createHeader();
            header.name().setString("Header" + i);
            header.getValue().setString("Value" + i);
        }

        assertEquals(5, headers.size());

        // 리사이클 후 크기 확인
        headers.recycle();
        assertEquals(0, headers.size());

        // 리사이클 후 다시 헤더 생성 가능한지 확인
        HeaderField newHeader = headers.createHeader();
        newHeader.name().setString("NewHeader");
        newHeader.getValue().setString("NewValue");

        assertEquals(1, headers.size());
        assertEquals("NewValue", headers.getHeader("NewHeader"));
    }

    @Test
    void createHeaderReturnsUniqueInstances() {
        HeaderField header1 = headers.createHeader();
        HeaderField header2 = headers.createHeader();

        assertNotSame(header1, header2);
        assertNotNull(header1);
        assertNotNull(header2);
    }

    @Test
    void arrayExpansionPreservesExistingHeaders() {
        // 기본 크기(8)만큼 헤더 생성
        for (int i = 0; i < 8; i++) {
            HeaderField header = headers.createHeader();
            header.name().setString("Header" + i);
            header.getValue().setString("Value" + i);
        }

        // 첫 번째 헤더 확인
        assertEquals("Value0", headers.getHeader("Header0"));

        // 배열 확장을 유발하는 9번째 헤더 생성
        HeaderField header9 = headers.createHeader();
        header9.name().setString("Header8");
        header9.getValue().setString("Value8");

        // 기존 헤더들이 여전히 유효한지 확인
        assertEquals("Value0", headers.getHeader("Header0"));
        assertEquals("Value7", headers.getHeader("Header7"));
        assertEquals("Value8", headers.getHeader("Header8"));
        assertEquals(9, headers.size());
    }

    @Test
    void emptyHeaderNameAndValueHandling() {
        HeaderField header = headers.createHeader();
        header.name().setString("");
        header.getValue().setString("");

        HeaderBytes value = headers.getValue("");
        assertNotNull(value);
        assertEquals("", value.toString());

        String headerValue = headers.getHeader("");
        assertEquals("", headerValue);
    }

    @Test
    void headerWithNullValue() {
        HeaderField header = headers.createHeader();
        header.name().setString("Test-Header");
        // getValue()는 기본적으로 빈 MessageBytes를 반환하므로 null 값 설정 테스트

        HeaderBytes value = headers.getValue("Test-Header");
        assertNotNull(value);

        String headerValue = headers.getHeader("Test-Header");
        // MessageBytes의 toString() 구현에 따라 결과가 달라질 수 있음
        assertNotNull(headerValue);
    }
}