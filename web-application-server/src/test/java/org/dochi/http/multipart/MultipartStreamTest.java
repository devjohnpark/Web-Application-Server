package org.dochi.http.multipart;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class MultipartStreamTest {
    private MultipartStream multipartStream;

    @Test
    void readCRLFLineValidInput() throws IOException {
        String input = "Hello\r\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        multipartStream = new MultipartStream(inputStream);

        byte[] result = multipartStream.readCRLFLine(100);

        assertArrayEquals("Hello".getBytes(), result);
    }

    @Test
    void readCRLFLineEmptyStream() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        multipartStream = new MultipartStream(inputStream);

        byte[] result = multipartStream.readCRLFLine(100);

        assertNull(result);
    }

    @Test
    void readCRLFLineNoCRLF() throws IOException {
        String input = "NoCRLF";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        multipartStream = new MultipartStream(inputStream);

        byte[] result = multipartStream.readCRLFLine(100);

        assertNull(result);
    }

    @Test
    void readCRLFLineExceedsMaxSize() {
        String input = "ThisIsTooLong\r\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        multipartStream = new MultipartStream(inputStream);
        assertThrows(IllegalStateException.class, () -> multipartStream.readCRLFLine(5));
    }

    @Test
    void readCRLFLineMultipleLines() throws IOException {
        String input = "First\r\nSecond\r\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        multipartStream = new MultipartStream(inputStream);

        byte[] firstLine = multipartStream.readCRLFLine(100);
        byte[] secondLine = multipartStream.readCRLFLine(100);

        assertArrayEquals("First".getBytes(), firstLine, "Should return first line content without CRLF");
        assertArrayEquals("Second".getBytes(), secondLine, "Should return second line content without CRLF");
    }

    @Test
    void readCRLFLineSingleCR() throws IOException {
        String input = "\r";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        multipartStream = new MultipartStream(inputStream);

        byte[] result = multipartStream.readCRLFLine(100);

        assertNull(result);
    }

    @Test
    void readCRLFLineEmptyLine() throws IOException {
        String input = "\r\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        multipartStream = new MultipartStream(inputStream);

        byte[] result = multipartStream.readCRLFLine(100);

        assertArrayEquals(new byte[0], result);
    }
}