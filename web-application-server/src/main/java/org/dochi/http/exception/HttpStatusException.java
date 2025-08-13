package org.dochi.http.exception;

import org.dochi.http.response.HttpStatus;

public class HttpStatusException extends RuntimeException {
    private final HttpStatus httpStatus;

    public HttpStatusException(HttpStatus status) {
        this(status, status.getDescription());
    }

    public HttpStatusException(HttpStatus status, String reason) {
        super(reason);
        this.httpStatus = status;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
