package org.dochi.internal.buffer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class ByteChunkTest {

    private ByteChunk byteChunk;

    @BeforeEach
    void setUp() {
        byteChunk = new ByteChunk();
    }

    @Test
    void defaultConstructor() {
        // Given & When
        ByteChunk chunk = new ByteChunk();

        // Then
        assertEquals(StandardCharsets.ISO_8859_1, chunk.getCharset());
        assertNull(chunk.getBuffer());
        assertEquals(0, chunk.getStart());
        assertEquals(0, chunk.getEnd());
        assertEquals(0, chunk.getLength());
    }

    @Test
    void setBytesNormal() {
        // Given
        byte[] data = "Hello World".getBytes();
        int offset = 2;
        int length = 5;

        // When
        byteChunk.setBytes(data, offset, length);

        // Then
        assertSame(data, byteChunk.getBuffer());
        assertEquals(offset, byteChunk.getStart());
        assertEquals(offset + length, byteChunk.getEnd());
        assertEquals(length, byteChunk.getLength());
    }

    @Test
    void setBytesEmpty() {
        // Given
        byte[] data = new byte[0];

        // When
        byteChunk.setBytes(data, 0, 0);

        // Then
        assertEquals(0, byteChunk.getLength());
    }

    @Test
    void setBytesFullLength() {
        // Given
        byte[] data = "Test".getBytes();

        // When
        byteChunk.setBytes(data, 0, data.length);

        // Then
        assertEquals(0, byteChunk.getStart());
        assertEquals(data.length, byteChunk.getEnd());
        assertEquals(data.length, byteChunk.getLength());
    }

    @Test
    void setCharsetNull() {
        assertThrows(IllegalArgumentException.class, () -> byteChunk.setCharset(null));
    }

    @Test
    void recycle() {
        // Given
        byte[] data = "test data".getBytes();
        byteChunk.setBytes(data, 2, 4);

        // When
        byteChunk.recycle();

        // Then
        assertEquals(0, byteChunk.getStart());
        assertEquals(0, byteChunk.getEnd());
        assertEquals(0, byteChunk.getLength());
        // buffer는 그대로 유지되어야 함
        assertSame(data, byteChunk.getBuffer());
    }

    @Test
    void toStringNormal() {
        // Given
        String original = "Hello";
        byte[] data = original.getBytes(StandardCharsets.ISO_8859_1);
        byteChunk.setBytes(data, 0, data.length);

        // When
        String result = byteChunk.toString();

        // Then
        assertEquals(original, result);
    }

    @Test
    void toStringPartial() {
        // Given
        byte[] data = "Hello World".getBytes(StandardCharsets.ISO_8859_1);
        byteChunk.setBytes(data, 6, 5); // "World"

        // When
        String result = byteChunk.toString();

        // Then
        assertEquals("World", result);
    }

    @Test
    void toStringEmpty() {
        // Given
        byte[] data = "test".getBytes();
        byteChunk.setBytes(data, 0, 0);

        // When
        String result = byteChunk.toString();

        // Then
        assertEquals("", result);
    }

    @Test
    void toStringUTF8() {
        // Given
        String original = "안녕하세요";
        byteChunk.setCharset(StandardCharsets.UTF_8);
        byte[] data = original.getBytes(StandardCharsets.UTF_8);
        byteChunk.setBytes(data, 0, data.length);

        // When
        String result = byteChunk.toString();

        // Then
        assertEquals(original, result);
    }

    @Test
    void toIntValid() {
        // Given
        byte[] data = "12345".getBytes();
        byteChunk.setBytes(data, 0, data.length);

        // When
        int result = byteChunk.toInt();

        // Then
        assertEquals(12345, result);
    }

    @Test
    void toIntZero() {
        // Given
        byte[] data = "0".getBytes();
        byteChunk.setBytes(data, 0, data.length);

        // When
        int result = byteChunk.toInt();

        // Then
        assertEquals(0, result);
    }

    @Test
    void toIntPartial() {
        // Given
        byte[] data = "abc123def".getBytes();
        byteChunk.setBytes(data, 3, 3); // "123"

        // When
        int result = byteChunk.toInt();

        // Then
        assertEquals(123, result);
    }

    @Test
    void toIntInvalidCharacter() {
        // Given
        byte[] data = "12a45".getBytes();
        byteChunk.setBytes(data, 0, data.length);

        // When & Then
        NumberFormatException exception = assertThrows(
                NumberFormatException.class,
                () -> byteChunk.toInt()
        );
        assertTrue(exception.getMessage().contains("Invalid digit at index"));
    }

    @Test
    void toIntEmpty() {
        // Given
        byte[] data = "test".getBytes();
        byteChunk.setBytes(data, 0, 0);

        // When
        int result = byteChunk.toInt();

        // Then
        assertEquals(0, result);
    }

    @Test
    void toIntSpecialCharacter() {
        // Given
        byte[] data = "12-34".getBytes();
        byteChunk.setBytes(data, 0, data.length);

        // When & Then
        assertThrows(NumberFormatException.class, () -> byteChunk.toInt());
    }

    @Test
    void equalsIgnoreCaseSame() {
        // Given
        byte[] data = "Hello".getBytes();
        byteChunk.setBytes(data, 0, data.length);

        // When & Then
        assertTrue(byteChunk.equalsIgnoreCase("hello"));
        assertTrue(byteChunk.equalsIgnoreCase("HELLO"));
        assertTrue(byteChunk.equalsIgnoreCase("Hello"));
        assertTrue(byteChunk.equalsIgnoreCase("HeLLo"));
    }

    @Test
    void equalsIgnoreCaseDifferent() {
        // Given
        byte[] data = "Hello".getBytes();
        byteChunk.setBytes(data, 0, data.length);

        // When & Then
        assertFalse(byteChunk.equalsIgnoreCase("World"));
        assertFalse(byteChunk.equalsIgnoreCase("Hi"));
    }

    @Test
    void equalsIgnoreCaseDifferentLength() {
        // Given
        byte[] data = "Hello".getBytes();
        byteChunk.setBytes(data, 0, data.length);

        // When & Then
        assertFalse(byteChunk.equalsIgnoreCase("HelloWorld"));
        assertFalse(byteChunk.equalsIgnoreCase("Hi"));
        assertFalse(byteChunk.equalsIgnoreCase(""));
    }

    @Test
    void equalsIgnoreCaseNull() {
        // Given
        byte[] data = "Hello".getBytes();
        byteChunk.setBytes(data, 0, data.length);

        // When & Then
        assertFalse(byteChunk.equalsIgnoreCase(null));
    }

    @Test
    void equalsIgnoreCaseNullBuffer() {
        // Given - buffer 설정하지 않음

        // When & Then
        assertFalse(byteChunk.equalsIgnoreCase("test"));
    }

    @Test
    void equalsIgnoreCaseEmpty() {
        // Given
        byte[] data = "test".getBytes();
        byteChunk.setBytes(data, 0, 0); // 길이 0

        // When & Then
        assertTrue(byteChunk.equalsIgnoreCase(""));
        assertFalse(byteChunk.equalsIgnoreCase("test"));
    }

    @Test
    void equalsIgnoreCaseWithNumbersAndSpecialChars() {
        // Given
        byte[] data = "Test123!".getBytes();
        byteChunk.setBytes(data, 0, data.length);

        // When & Then
        assertTrue(byteChunk.equalsIgnoreCase("test123!"));
        assertTrue(byteChunk.equalsIgnoreCase("TEST123!"));
        assertFalse(byteChunk.equalsIgnoreCase("test124!"));
    }

    @Test
    void equalsIgnoreCasePartial() {
        // Given
        byte[] data = "Hello World".getBytes();
        byteChunk.setBytes(data, 6, 5); // "World"

        // When & Then
        assertTrue(byteChunk.equalsIgnoreCase("world"));
        assertTrue(byteChunk.equalsIgnoreCase("WORLD"));
        assertFalse(byteChunk.equalsIgnoreCase("Hello"));
    }
}
