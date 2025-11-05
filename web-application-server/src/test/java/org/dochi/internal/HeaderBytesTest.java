package org.dochi.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class HeaderBytesTest {
    private HeaderBytes headerBytes;

    @BeforeEach
    void setUp() {
        headerBytes = new HeaderBytes();
    }

    @Test
    void newInstance() {
        // When
        HeaderBytes mb = new HeaderBytes();

        // Then
        assertNotNull(mb);
        assertTrue(mb.isNull());
        assertEquals(0, mb.getLength());
        assertNotNull(mb.getByteChunk());
    }

    @Test
    void setBytesNormal() {
        // Given
        byte[] data = "Hello World".getBytes();

        // When
        headerBytes.setBytes(data, 0, data.length);

        // Then
        assertFalse(headerBytes.isNull());
        assertEquals(data.length, headerBytes.getLength());
        assertSame(data, headerBytes.getByteChunk().getBuffer());
    }

    @Test
    void setBytesPartial() {
        // Given
        byte[] data = "Hello World".getBytes();
        int offset = 6;
        int length = 5;

        // When
        headerBytes.setBytes(data, offset, length);

        // Then
        assertFalse(headerBytes.isNull());
        assertEquals(length, headerBytes.getLength());
        assertEquals("World", headerBytes.toString());
    }

    @Test
    void setBytesEmpty() {
        // Given
        byte[] data = new byte[0];

        // When
        headerBytes.setBytes(data, 0, 0);

        // Then
        assertFalse(headerBytes.isNull());
        assertEquals(0, headerBytes.getLength());
    }

    @Test
    void setStringNormal() {
        // Given
        String testString = "Hello World";

        // When
        headerBytes.setString(testString);

        // Then
        assertFalse(headerBytes.isNull());
        assertEquals(testString.length(), headerBytes.getLength());
        assertEquals(testString, headerBytes.toString());
    }

    @Test
    void setStringEmpty() {
        // Given
        String emptyString = "";

        // When
        headerBytes.setString(emptyString);

        // Then
        assertFalse(headerBytes.isNull());
        assertEquals(0, headerBytes.getLength());
        assertEquals("", headerBytes.toString());
    }

    @Test
    void setStringNull() {
        // When
        headerBytes.setString(null);

        // Then
        assertTrue(headerBytes.isNull());
        assertEquals(0, headerBytes.getLength());
    }

    @Test
    void recycleAfterSetBytes() {
        // Given
        byte[] data = "test data".getBytes();
        headerBytes.setBytes(data, 0, data.length);

        // When
        headerBytes.recycle();

        // Then
        assertTrue(headerBytes.isNull());
        assertEquals(0, headerBytes.getLength());
    }

    @Test
    void recycleAfterSetString() {
        // Given
        headerBytes.setString("test string");

        // When
        headerBytes.recycle();

        // Then
        assertTrue(headerBytes.isNull());
        assertEquals(0, headerBytes.getLength());
    }

    @Test
    void recycleAfterToInt() {
        // Given
        byte[] data = "123".getBytes();
        headerBytes.setBytes(data, 0, data.length);
        headerBytes.toInt(); // 캐시 생성

        // When
        headerBytes.recycle();

        // Then
        assertTrue(headerBytes.isNull());
        // 새로운 데이터 설정 후 toInt 재계산 확인
        byte[] newData = "456".getBytes();
        headerBytes.setBytes(newData, 0, newData.length);
        assertEquals(456, headerBytes.toInt());
    }

    @Test
    void toStringFromBytes() {
        // Given
        String original = "Hello World";
        byte[] data = original.getBytes();
        headerBytes.setBytes(data, 0, data.length);

        // When
        String result = headerBytes.toString();

        // Then
        assertEquals(original, result);
        // 두 번째 호출에서도 동일한 결과
        assertEquals(original, headerBytes.toString());
    }

    @Test
    void toStringFromString() {
        // Given
        String original = "Test String";
        headerBytes.setString(original);

        // When
        String result = headerBytes.toString();

        // Then
        assertEquals(original, result);
    }

    @Test
    void toStringNull() {
        // When
        String result = headerBytes.toString();

        // Then
        assertEquals("", result);
    }

    @Test
    void toStringWithCharset() {
        // Given
        String koreanText = "안녕하세요";
        byte[] utf8Data = koreanText.getBytes(StandardCharsets.UTF_8);
        headerBytes.setCharset(StandardCharsets.UTF_8);
        headerBytes.setBytes(utf8Data, 0, utf8Data.length);

        // When
        String result = headerBytes.toString();

        // Then
        assertEquals(koreanText, result);
    }

    @Test
    void toIntNormal() {
        // Given
        byte[] data = "12345".getBytes();
        headerBytes.setBytes(data, 0, data.length);

        // When
        int result = headerBytes.toInt();

        // Then
        assertEquals(12345, result);
        // 캐시 확인 - 두 번째 호출
        assertEquals(12345, headerBytes.toInt());
    }

    @Test
    void toIntZero() {
        // Given
        byte[] data = "0".getBytes();
        headerBytes.setBytes(data, 0, data.length);

        // When
        int result = headerBytes.toInt();

        // Then
        assertEquals(0, result);
    }

    @Test
    void toIntInvalid() {
        // Given
        byte[] data = "12a34".getBytes();
        headerBytes.setBytes(data, 0, data.length);

        // When & Then
        assertThrows(NumberFormatException.class, () -> headerBytes.toInt());
    }

    @Test
    void toIntEmpty() {
        // Given
        byte[] data = new byte[0];
        headerBytes.setBytes(data, 0, 0);

        // When
        int result = headerBytes.toInt();

        // Then
        assertEquals(0, result);
    }

    @Test
    void getLengthByteType() {
        // Given
        byte[] data = "test".getBytes();
        headerBytes.setBytes(data, 2, 2);

        // When
        int length = headerBytes.getLength();

        // Then
        assertEquals(2, length);
    }

    @Test
    void getLengthStringType() {
        // Given
        headerBytes.setString("Hello");

        // When
        int length = headerBytes.getLength();

        // Then
        assertEquals(5, length);
    }

    @Test
    void getLengthNullType() {
        // When
        int length = headerBytes.getLength();

        // Then
        assertEquals(0, length);
    }

    @Test
    void getLengthAfterConversion() {
        // Given
        byte[] data = "test".getBytes();
        headerBytes.setBytes(data, 0, data.length);
        headerBytes.toString(); // 문자열로 변환

        // When
        int length = headerBytes.getLength();

        // Then
        assertEquals(4, length);
    }

    @Test
    void toByteFromString() {
        // Given
        String testString = "Hello";
        headerBytes.setString(testString);

        // When
        headerBytes.toByte();

        // Then
        assertEquals(testString, headerBytes.toString());
        assertEquals(testString.length(), headerBytes.getLength());
    }

    @Test
    void toByteUTF8() {
        // Given
        String koreanText = "안녕";
        headerBytes.setCharset(StandardCharsets.UTF_8);
        headerBytes.setString(koreanText);

        // When
        headerBytes.toByte();

        // Then
        assertEquals(koreanText, headerBytes.toString());
    }

    @Test
    void toByteNull() {
        // Given
        headerBytes.setString(null);

        // When & Then
        assertDoesNotThrow(() -> headerBytes.toByte());
    }

    @Test
    void toByteEmpty() {
        // Given
        headerBytes.setString("");

        // When
        headerBytes.toByte();

        // Then
        assertEquals("", headerBytes.toString());
        assertEquals(0, headerBytes.getLength());
    }

    @Test
    void equalsIgnoreCaseByteType() {
        // Given
        byte[] data = "Hello".getBytes();
        headerBytes.setBytes(data, 0, data.length);

        // When & Then
        assertTrue(headerBytes.equalsIgnoreCase("hello"));
        assertTrue(headerBytes.equalsIgnoreCase("HELLO"));
        assertTrue(headerBytes.equalsIgnoreCase("Hello"));
        assertFalse(headerBytes.equalsIgnoreCase("World"));
    }

    @Test
    void equalsIgnoreCaseStringType() {
        // Given
        headerBytes.setString("Hello");

        // When & Then
        assertTrue(headerBytes.equalsIgnoreCase("hello"));
        assertTrue(headerBytes.equalsIgnoreCase("HELLO"));
        assertTrue(headerBytes.equalsIgnoreCase("Hello"));
        assertFalse(headerBytes.equalsIgnoreCase("World"));
    }

    @Test
    void equalsIgnoreCaseNullType() {
        // When & Then
        assertFalse(headerBytes.equalsIgnoreCase("test"));
        assertFalse(headerBytes.equalsIgnoreCase(null));
    }

    @Test
    void equalsIgnoreCaseStringNull() {
        // Given
        headerBytes.setString(null);

        // When & Then
        assertFalse(headerBytes.equalsIgnoreCase(null));
        assertFalse(headerBytes.equalsIgnoreCase("test"));
    }

    @Test
    void equalsIgnoreCaseAfterConversion() {
        // Given
        byte[] data = "Test".getBytes();
        headerBytes.setBytes(data, 0, data.length);
        headerBytes.toString(); // 문자열로 변환

        // When & Then
        assertTrue(headerBytes.equalsIgnoreCase("test"));
        assertTrue(headerBytes.equalsIgnoreCase("TEST"));
    }

    }