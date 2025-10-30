package org.dochi.internal.buffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeaderFieldTest {

    @Test
    void initialNameAndValueAreEmpty() {
        HeaderField field = new HeaderField();
        assertTrue(field.name().isNull());
        assertTrue(field.getValue().isNull());
    }

    @Test
    void nameAndValueCanBeSetAndRetrieved() {
        HeaderField field = new HeaderField();
        field.name().setString("Content-Type");
        field.getValue().setString("text/html");

        assertEquals("Content-Type", field.name().toString());
        assertEquals("text/html", field.getValue().toString());
    }

    @Test
    void recycleClearsNameAndValue() {
        HeaderField field = new HeaderField();
        field.name().setString("Host");
        field.getValue().setString("localhost");

        field.recycle();

        assertTrue(field.name().isNull());
        assertTrue(field.getValue().isNull());
    }

    @Test
    void toStringReturnsFormattedHeader() {
        HeaderField field = new HeaderField();
        field.name().setString("Accept");
        field.getValue().setString("application/json");

        assertEquals("Accept: application/json", field.toString());
    }
}
