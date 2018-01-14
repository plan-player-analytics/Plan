/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.webapi;

/**
 * Thrown when WebAPI gets a 403 response.
 *
 * @author Rsl1122
 */
public class WebAPIForbiddenException extends WebAPIFailException {
    public WebAPIForbiddenException(String url) {
        super("Forbidden: " + url);
    }
}