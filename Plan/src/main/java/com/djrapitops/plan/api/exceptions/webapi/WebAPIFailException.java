/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.webapi;

/**
 * Group of WebAPIExceptions that can be considered a failed connection state on some occasions.
 *
 * @author Rsl1122
 */
public class WebAPIFailException extends WebAPIException {

    public WebAPIFailException() {
    }

    public WebAPIFailException(String message) {
        super(message);
    }

    public WebAPIFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebAPIFailException(Throwable cause) {
        super(cause);
    }
}