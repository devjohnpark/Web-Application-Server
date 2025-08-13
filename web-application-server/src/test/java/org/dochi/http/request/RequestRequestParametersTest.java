package org.dochi.http.request;

import org.dochi.http.request.data.RequestParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestRequestParametersTest {

    private RequestParameters parameters;

    @BeforeEach
    void setUp() {
        parameters = new RequestParameters();
    }

    @Test
    void addRequestParameters_Line() {
        String queryStringWithUrlEncoded = "name=john%20park&age=20";
        parameters.addRequestParameters(queryStringWithUrlEncoded);
        assertThat(parameters.getRequestParameterValue("name")).isEqualTo("john park");
        assertThat(parameters.getRequestParameterValue("age")).isEqualTo("20");
    }

    @Test
    void addParameters_formUrlEncoded() {
        String queryStringWithUrlEncoded = "name=john+park&age=20";
        parameters.addRequestParameters(queryStringWithUrlEncoded);
        assertThat(parameters.getRequestParameterValue("name")).isEqualTo("john park");
        assertThat(parameters.getRequestParameterValue("age")).isEqualTo("20");
    }


    @Test
    void addParameters_null() {
        parameters.addRequestParameters(null);
        assertThat(parameters.getRequestParameterValue("name")).isNull();
        assertThat(parameters.getRequestParameterValue("age")).isNull();
    }

    @Test
    void addParameters_empty() {
        parameters.addRequestParameters("");
        assertThat(parameters.getRequestParameterValue("")).isNull();
        assertThat(parameters.getRequestParameterValue("")).isNull();
    }
}