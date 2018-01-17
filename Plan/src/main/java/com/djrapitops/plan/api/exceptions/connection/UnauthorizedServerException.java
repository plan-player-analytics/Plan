/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.connection;

/**
 * Thrown when Connection gets a 412 response due to ServerUUID not being in the database.
 *
 * @author Rsl1122
 */
public class UnauthorizedServerException extends WebFailException {

    public UnauthorizedServerException() {
    }

    public UnauthorizedServerException(String message) {
        super(message);
    }

    public UnauthorizedServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedServerException(Throwable cause) {
        super(cause);
    }
}