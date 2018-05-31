package com.djrapitops.plan.system.webserver.response;

/**
 * Enum for HTTP response codes.
 *
 * @author Rsl1122
 */
public enum ResponseCode {
    NONE(0),
    CONNECTION_REFUSED(-1),
    SUCCESS(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    PRECONDITION_FAILED(412),
    INTERNAL_ERROR(500),
    GATEWAY_ERROR(504);

    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}