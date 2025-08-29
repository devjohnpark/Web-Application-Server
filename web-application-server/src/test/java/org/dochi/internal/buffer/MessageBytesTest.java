package org.dochi.internal.buffer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class MessageBytesTest {
    private MessageBytes messageBytes;

    @BeforeEach
    void setUp() {
        messageBytes = MessageBytes.newInstance();
    }

    @Test
    void newInstance() {
        // When
        MessageBytes mb = MessageBytes.newInstance();

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
        messageBytes.setBytes(data, 0, data.length);

        // Then
        assertFalse(messageBytes.isNull());
        assertEquals(data.length, messageBytes.getLength());
        assertSame(data, messageBytes.getByteChunk().getBuffer());
    }

    @Test
    void setBytesPartial() {
        // Given
        byte[] data = "Hello World".getBytes();
        int offset = 6;
        int length = 5;

        // When
        messageBytes.setBytes(data, offset, length);

        // Then
        assertFalse(messageBytes.isNull());
        assertEquals(length, messageBytes.getLength());
        assertEquals("World", messageBytes.toString());
    }

    @Test
    void setBytesEmpty() {
        // Given
        byte[] data = new byte[0];

        // When
        messageBytes.setBytes(data, 0, 0);

        // Then
        assertFalse(messageBytes.isNull());
        assertEquals(0, messageBytes.getLength());
    }

    @Test
    void setStringNormal() {
        // Given
        String testString = "Hello World";

        // When
        messageBytes.setString(testString);

        // Then
        assertFalse(messageBytes.isNull());
        assertEquals(testString.length(), messageBytes.getLength());
        assertEquals(testString, messageBytes.toString());
    }

    @Test
    void setStringEmpty() {
        // Given
        String emptyString = "";

        // When
        messageBytes.setString(emptyString);

        // Then
        assertFalse(messageBytes.isNull());
        assertEquals(0, messageBytes.getLength());
        assertEquals("", messageBytes.toString());
    }

    @Test
    void setStringNull() {
        // When
        messageBytes.setString(null);

        // Then
        assertTrue(messageBytes.isNull());
        assertEquals(0, messageBytes.getLength());
    }

    @Test
    void recycleAfterSetBytes() {
        // Given
        byte[] data = "test data".getBytes();
        messageBytes.setBytes(data, 0, data.length);

        // When
        messageBytes.recycle();

        // Then
        assertTrue(messageBytes.isNull());
        assertEquals(0, messageBytes.getLength());
    }

    @Test
    void recycleAfterSetString() {
        // Given
        messageBytes.setString("test string");

        // When
        messageBytes.recycle();

        // Then
        assertTrue(messageBytes.isNull());
        assertEquals(0, messageBytes.getLength());
    }

    @Test
    void recycleAfterToInt() {
        // Given
        byte[] data = "123".getBytes();
        messageBytes.setBytes(data, 0, data.length);
        messageBytes.toInt(); // 캐시 생성

        // When
        messageBytes.recycle();

        // Then
        assertTrue(messageBytes.isNull());
        // 새로운 데이터 설정 후 toInt 재계산 확인
        byte[] newData = "456".getBytes();
        messageBytes.setBytes(newData, 0, newData.length);
        assertEquals(456, messageBytes.toInt());
    }

    @Test
    void toStringFromBytes() {
        // Given
        String original = "Hello World";
        byte[] data = original.getBytes();
        messageBytes.setBytes(data, 0, data.length);

        // When
        String result = messageBytes.toString();

        // Then
        assertEquals(original, result);
        // 두 번째 호출에서도 동일한 결과
        assertEquals(original, messageBytes.toString());
    }

    @Test
    void toStringFromString() {
        // Given
        String original = "Test String";
        messageBytes.setString(original);

        // When
        String result = messageBytes.toString();

        // Then
        assertEquals(original, result);
    }

    @Test
    void toStringNull() {
        // When
        String result = messageBytes.toString();

        // Then
        assertEquals("", result);
    }

    @Test
    void toStringWithCharset() {
        // Given
        String koreanText = "안녕하세요";
        byte[] utf8Data = koreanText.getBytes(StandardCharsets.UTF_8);
        messageBytes.setCharset(StandardCharsets.UTF_8);
        messageBytes.setBytes(utf8Data, 0, utf8Data.length);

        // When
        String result = messageBytes.toString();

        // Then
        assertEquals(koreanText, result);
    }

    @Test
    void toIntNormal() {
        // Given
        byte[] data = "12345".getBytes();
        messageBytes.setBytes(data, 0, data.length);

        // When
        int result = messageBytes.toInt();

        // Then
        assertEquals(12345, result);
        // 캐시 확인 - 두 번째 호출
        assertEquals(12345, messageBytes.toInt());
    }

    @Test
    void toIntZero() {
        // Given
        byte[] data = "0".getBytes();
        messageBytes.setBytes(data, 0, data.length);

        // When
        int result = messageBytes.toInt();

        // Then
        assertEquals(0, result);
    }

    @Test
    void toIntInvalid() {
        // Given
        byte[] data = "12a34".getBytes();
        messageBytes.setBytes(data, 0, data.length);

        // When & Then
        assertThrows(NumberFormatException.class, () -> messageBytes.toInt());
    }

    @Test
    void toIntEmpty() {
        // Given
        byte[] data = new byte[0];
        messageBytes.setBytes(data, 0, 0);

        // When
        int result = messageBytes.toInt();

        // Then
        assertEquals(0, result);
    }

    @Test
    void getLengthByteType() {
        // Given
        byte[] data = "test".getBytes();
        messageBytes.setBytes(data, 2, 2);

        // When
        int length = messageBytes.getLength();

        // Then
        assertEquals(2, length);
    }

    @Test
    void getLengthStringType() {
        // Given
        messageBytes.setString("Hello");

        // When
        int length = messageBytes.getLength();

        // Then
        assertEquals(5, length);
    }

    @Test
    void getLengthNullType() {
        // When
        int length = messageBytes.getLength();

        // Then
        assertEquals(0, length);
    }

    @Test
    void getLengthAfterConversion() {
        // Given
        byte[] data = "test".getBytes();
        messageBytes.setBytes(data, 0, data.length);
        messageBytes.toString(); // 문자열로 변환

        // When
        int length = messageBytes.getLength();

        // Then
        assertEquals(4, length);
    }

    @Test
    void toByteFromString() {
        // Given
        String testString = "Hello";
        messageBytes.setString(testString);

        // When
        messageBytes.toByte();

        // Then
        assertEquals(testString, messageBytes.toString());
        assertEquals(testString.length(), messageBytes.getLength());
    }

    @Test
    void toByteUTF8() {
        // Given
        String koreanText = "안녕";
        messageBytes.setCharset(StandardCharsets.UTF_8);
        messageBytes.setString(koreanText);

        // When
        messageBytes.toByte();

        // Then
        assertEquals(koreanText, messageBytes.toString());
    }

    @Test
    void toByteNull() {
        // Given
        messageBytes.setString(null);

        // When & Then
        assertDoesNotThrow(() -> messageBytes.toByte());
    }

    @Test
    void toByteEmpty() {
        // Given
        messageBytes.setString("");

        // When
        messageBytes.toByte();

        // Then
        assertEquals("", messageBytes.toString());
        assertEquals(0, messageBytes.getLength());
    }

    @Test
    void equalsIgnoreCaseByteType() {
        // Given
        byte[] data = "Hello".getBytes();
        messageBytes.setBytes(data, 0, data.length);

        // When & Then
        assertTrue(messageBytes.equalsIgnoreCase("hello"));
        assertTrue(messageBytes.equalsIgnoreCase("HELLO"));
        assertTrue(messageBytes.equalsIgnoreCase("Hello"));
        assertFalse(messageBytes.equalsIgnoreCase("World"));
    }

    @Test
    void equalsIgnoreCaseStringType() {
        // Given
        messageBytes.setString("Hello");

        // When & Then
        assertTrue(messageBytes.equalsIgnoreCase("hello"));
        assertTrue(messageBytes.equalsIgnoreCase("HELLO"));
        assertTrue(messageBytes.equalsIgnoreCase("Hello"));
        assertFalse(messageBytes.equalsIgnoreCase("World"));
    }

    @Test
    void equalsIgnoreCaseNullType() {
        // When & Then
        assertFalse(messageBytes.equalsIgnoreCase("test"));
        assertFalse(messageBytes.equalsIgnoreCase(null));
    }

    @Test
    void equalsIgnoreCaseStringNull() {
        // Given
        messageBytes.setString(null);

        // When & Then
        assertFalse(messageBytes.equalsIgnoreCase(null));
        assertFalse(messageBytes.equalsIgnoreCase("test"));
    }

    @Test
    void equalsIgnoreCaseAfterConversion() {
        // Given
        byte[] data = "Test".getBytes();
        messageBytes.setBytes(data, 0, data.length);
        messageBytes.toString(); // 문자열로 변환

        // When & Then
        assertTrue(messageBytes.equalsIgnoreCase("test"));
        assertTrue(messageBytes.equalsIgnoreCase("TEST"));
    }

    }