/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.webserver.response.ResponseCode;

/**
 * Thrown when Connection fails to connect to an address.
 *
 * @author Rsl1122
 */
public class ConnectionFailException extends WebException {

    public ConnectionFailException(String message, Throwable cause) {
        super(message, cause, ResponseCode.CONNECTION_REFUSED);
    }

    public ConnectionFailException(Throwable cause) {
        super(cause, ResponseCode.CONNECTION_REFUSED);
    }
}
