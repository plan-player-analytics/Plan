/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.connection;

/**
 * Thrown when Connection fails to connect to an address.
 *
 * @author Rsl1122
 */
public class ConnectionFailException extends WebException {

    public ConnectionFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionFailException(Throwable cause) {
        super(cause);
    }
}