/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

/**
 * Thrown when ConnectionSystem can not find any servers to send request to.
 *
 * @author Rsl1122
 */
public class NoServersException extends WebException {

    public NoServersException(String message) {
        super(message);
    }

    public NoServersException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoServersException(Throwable cause) {
        super(cause);
    }
}
