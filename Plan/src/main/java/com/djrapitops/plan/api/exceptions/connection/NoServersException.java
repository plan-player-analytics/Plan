/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.connection;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class NoServersException extends WebException {

    public NoServersException() {
    }

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