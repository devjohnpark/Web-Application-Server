package org.dochi.http.response;

public enum HttpStatus {
    OK(200, "OK", ""),
    NO_CONTENT(204, "No Content", ""),
    BAD_REQUEST(400, "Bad Request", "The server cannot or will not process the request."),
    NOT_FOUND(404, "Not Found", "The requested resource could not be found."),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed", "The request method is known by the server but is not supported by the target resource."),
    LENGTH_REQUIRED(411, "Length Required", "The server refuses to accept the request without a defined Content-Length."),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large", "The request body is too large."),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large", "The server is unwilling to process the request because its header fields are too large."),
    NOT_IMPLEMENTED(501, "Not Implemented", "The request method is not supported by the server and cannot be handled."),
    REQUEST_TIMEOUT(408, "Request Timeout", "Failed to process request in time. Please try again."),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error", "The server was unable to complete your request. Please try again later.");

    private final int code;
    private final String codeMessage;
    private final String description;

    HttpStatus(int code, String codeMessage, String description) {
        this.code = code;
        this.codeMessage = codeMessage;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return codeMessage;
    }

    public String getDescription() {
        return description;
    }
}
