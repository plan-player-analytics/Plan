package com.djrapitops.plan.api.exceptions;

public class DBNoDataException extends DatabaseException {

    public DBNoDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DBNoDataException(Throwable cause) {
        super(cause);
    }

    public DBNoDataException(String message) {
        super(message);
    }
}
