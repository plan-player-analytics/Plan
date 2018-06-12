/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.webserver.response.ResponseCode;

/**
 * Thrown when Connection gets a 412 response due to ServerUUID not being in the database.
 *
 * @author Rsl1122
 */
public class UnauthorizedServerException extends WebFailException {

    public UnauthorizedServerException(String message) {
        super(message, ResponseCode.PRECONDITION_FAILED);
    }

    public UnauthorizedServerException(String message, Throwable cause) {
        super(message, cause, ResponseCode.PRECONDITION_FAILED);
    }

    public UnauthorizedServerException(Throwable cause) {
        super(cause, ResponseCode.PRECONDITION_FAILED);
    }
}
