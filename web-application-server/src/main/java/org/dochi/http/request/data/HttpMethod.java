package org.dochi.http.request.data;

public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    CONNECT("CONNECT");

    private final String method;

    HttpMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }

    public static HttpMethod fromString(String method) {
        for (HttpMethod httpMethod: HttpMethod.values()) {
            if (httpMethod.getMethod().equalsIgnoreCase(method)) {
                return httpMethod;
            }
        }
        throw new IllegalArgumentException("Invalid HTTP Method: " + method);
    }
}

