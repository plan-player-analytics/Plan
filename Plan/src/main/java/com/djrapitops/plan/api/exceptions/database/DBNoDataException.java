package com.djrapitops.plan.api.exceptions.database;

public class DBNoDataException extends DBException {

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
