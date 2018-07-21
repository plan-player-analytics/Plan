package com.djrapitops.plan.api.exceptions.database;

public class FatalDBException extends DBException {

    public FatalDBException(String message, Throwable cause) {
        super(message, cause);
    }

    public FatalDBException(Throwable cause) {
        super(cause);
    }

    public FatalDBException(String message) {
        super(message);
    }
}
