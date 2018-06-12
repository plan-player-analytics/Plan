/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.webserver.response.ResponseCode;

/**
 * Thrown when Connection gets a 403 response.
 *
 * @author Rsl1122
 */
public class ForbiddenException extends WebFailException {
    public ForbiddenException(String url) {
        super("Forbidden: " + url, ResponseCode.FORBIDDEN);
    }
}
