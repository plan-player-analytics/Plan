package com.djrapitops.plan.api.exceptions.database;

import java.sql.SQLException;

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

    public static DBOpException forCause(String sql, SQLException e) {
        return new DBOpException("SQL Failed: " + sql + "; " + e.getMessage(), e);
    }

}