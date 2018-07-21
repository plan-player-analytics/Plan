/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.database;

/**
 * Thrown when something goes wrong with the Database, generic exception.
 *
 * @author Rsl1122
 */
public class DBException extends Exception {

    public DBException(String message, Throwable cause) {
        super(message, cause);
    }

    public DBException(Throwable cause) {
        super(cause);
    }

    public DBException(String message) {
        super(message);
    }
}
