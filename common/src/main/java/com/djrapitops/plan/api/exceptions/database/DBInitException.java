/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.database;

/**
 * Thrown when something goes wrong with {@code Database#init}.
 *
 * @author Rsl1122
 */
public class DBInitException extends FatalDBException {

    public DBInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public DBInitException(Throwable cause) {
        super(cause);
    }

    public DBInitException(String message) {
        super(message);
    }
}
