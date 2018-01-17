/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.connection;

/**
 * Thrown when Connection gets a 403 response.
 *
 * @author Rsl1122
 */
public class WebForbiddenException extends WebFailException {
    public WebForbiddenException(String url) {
        super("Forbidden: " + url);
    }
}