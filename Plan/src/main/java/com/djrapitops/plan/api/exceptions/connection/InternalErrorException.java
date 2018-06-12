/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.webserver.response.ResponseCode;

/**
 * Thrown when Connection returns 500.
 *
 * @author Rsl1122
 */
public class InternalErrorException extends WebFailException {
    public InternalErrorException() {
        super("Internal Error occurred on receiving server", ResponseCode.INTERNAL_ERROR);
    }

    public InternalErrorException(String message, Throwable cause) {
        super(message, cause, ResponseCode.INTERNAL_ERROR);
    }
}
