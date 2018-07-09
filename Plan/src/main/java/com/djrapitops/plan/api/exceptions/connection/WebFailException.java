/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.webserver.response.ResponseCode;

/**
 * Group of WebExceptions that can be considered a failed connection state on some occasions.
 *
 * @author Rsl1122
 */
public class WebFailException extends WebException {

    public WebFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebFailException(String message, ResponseCode responseCode) {
        super(message, responseCode);
    }

    public WebFailException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause, responseCode);
    }

    public WebFailException(Throwable cause, ResponseCode responseCode) {
        super(cause, responseCode);
    }
}
