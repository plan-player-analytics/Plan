/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.webserver.response.ResponseCode;

/**
 * Thrown when connection is returned 401 Bad Request.
 *
 * @author Rsl1122
 */
public class BadRequestException extends WebException {

    public BadRequestException(String message) {
        super(message, ResponseCode.BAD_REQUEST);
    }
}
