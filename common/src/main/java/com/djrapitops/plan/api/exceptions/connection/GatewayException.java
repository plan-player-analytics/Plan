/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.webserver.response.ResponseCode;

/**
 * Thrown when InfoRequest can not connect to the other server.
 *
 * @author Rsl1122
 */
public class GatewayException extends WebException {

    public GatewayException(String message) {
        super(message, ResponseCode.GATEWAY_ERROR);
    }
}
