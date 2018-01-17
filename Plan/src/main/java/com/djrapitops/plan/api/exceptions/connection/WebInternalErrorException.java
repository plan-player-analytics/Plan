/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.connection;

/**
 * Thrown when Connection returns 500.
 *
 * @author Rsl1122
 */
public class WebInternalErrorException extends WebFailException {
    public WebInternalErrorException() {
        super("Internal Error occurred on receiving server");
    }
}