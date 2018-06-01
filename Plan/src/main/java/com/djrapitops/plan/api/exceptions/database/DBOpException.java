package com.djrapitops.plan.api.exceptions.database;

/**
 * Runtime exception for wrapping database errors.
 *
 * @author Rsl1122
 */
public class DBOpException extends RuntimeException {

    public DBOpException(String message) {
        super(message);
    }

    public DBOpException(String message, Throwable cause) {
        super(message, cause);
    }
}