/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.webapi;

/**
 * Thrown when WebAPI returns 404, usually when response is supposed to be false.
 *
 * @author Rsl1122
 */
public class WebAPINotFoundException extends WebAPIFailException {
    public WebAPINotFoundException() {
        super("Not Found");
    }
}