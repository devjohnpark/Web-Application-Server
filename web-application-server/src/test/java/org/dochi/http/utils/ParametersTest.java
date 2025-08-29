package org.dochi.http.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParametersTest {

    private Parameters parameters;

    @BeforeEach
    void init() {
        parameters = new Parameters();
    }

    @Test
    void addParameterStoresKeyValuePair() {
        parameters.addParameter("name", "john");
        assertEquals("john", parameters.getValue("name"));
    }

    @Test
    void addParameterIgnoresNullKey() {
        parameters.addParameter(null, "value");
        assertNull(parameters.getValue(null));
    }

    @Test
    void addParameterIgnoresNullValue() {
        parameters.addParameter("key", null);
        assertNull(parameters.getValue("key"));
    }

    @Test
    void addParameterIgnoresEmptyKey() {
        parameters.addParameter("", "value");
        assertNull(parameters.getValue(""));
    }

    @Test
    void addRequestParametersParsesQueryString() {
        parameters.addRequestParameters("name=john&age=30");
        assertEquals("john", parameters.getValue("name"));
        assertEquals("30", parameters.getValue("age"));
    }

    @Test
    void addRequestParametersIgnoresNullInput() {
        parameters.addRequestParameters(null);
        assertNull(parameters.getValue("any"));
    }

    @Test
    void addRequestParametersIgnoresEmptyInput() {
        parameters.addRequestParameters("");
        assertNull(parameters.getValue("any"));
    }

    @Test
    void addRequestParametersHandlesUrlEncodedValues() {
        parameters.addRequestParameters("name=John+Doe&city=New%20York");
        assertEquals("John Doe", parameters.getValue("name"));
        assertEquals("New York", parameters.getValue("city"));
    }

    @Test
    void recycleClearsAllStoredParameters() {
        parameters.addParameter("key1", "value1");
        parameters.addParameter("key2", "value2");
        parameters.recycle();

        assertNull(parameters.getValue("key1"));
        assertNull(parameters.getValue("key2"));
    }

    @Test
    void recycleDoesNothingWhenMapIsAlreadyEmpty() {
        // Should not throw any exceptions
        parameters.recycle();
    }
}
