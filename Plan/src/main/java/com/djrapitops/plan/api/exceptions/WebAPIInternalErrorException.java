/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions;

/**
 * Thrown when WebAPI returns 404, usually when response is supposed to be false.
 *
 * @author Rsl1122
 */
public class WebAPIInternalErrorException extends WebAPIFailException {
    public WebAPIInternalErrorException() {
        super("Internal Error occurred on receiving server");
    }
}