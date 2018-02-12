/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.connection;

/**
 * Group of WebExceptions that can be considered a failed connection state on some occasions.
 *
 * @author Rsl1122
 */
public class WebFailException extends WebException {

    public WebFailException() {
    }

    public WebFailException(String message) {
        super(message);
    }

    public WebFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebFailException(Throwable cause) {
        super(cause);
    }
}