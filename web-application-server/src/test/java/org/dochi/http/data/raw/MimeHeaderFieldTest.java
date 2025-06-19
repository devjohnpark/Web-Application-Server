package org.dochi.http.data.raw;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MimeHeaderFieldTest {

    @Test
    void initialNameAndValueAreEmpty() {
        MimeHeaderField field = new MimeHeaderField();
        assertTrue(field.getName().isNull());
        assertTrue(field.getValue().isNull());
    }

    @Test
    void nameAndValueCanBeSetAndRetrieved() {
        MimeHeaderField field = new MimeHeaderField();
        field.getName().setString("Content-Type");
        field.getValue().setString("text/html");

        assertEquals("Content-Type", field.getName().toString());
        assertEquals("text/html", field.getValue().toString());
    }

    @Test
    void recycleClearsNameAndValue() {
        MimeHeaderField field = new MimeHeaderField();
        field.getName().setString("Host");
        field.getValue().setString("localhost");

        field.recycle();

        assertTrue(field.getName().isNull());
        assertTrue(field.getValue().isNull());
    }

    @Test
    void toStringReturnsFormattedHeader() {
        MimeHeaderField field = new MimeHeaderField();
        field.getName().setString("Accept");
        field.getValue().setString("application/json");

        assertEquals("Accept: application/json", field.toString());
    }
}
