/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.webserver.response.ResponseCode;

/**
 * Thrown when Connection returns 404, when page is not found.
 *
 * @author Rsl1122
 */
public class NotFoundException extends WebFailException {
    public NotFoundException(String message) {
        super(message, ResponseCode.NOT_FOUND);
    }
}
