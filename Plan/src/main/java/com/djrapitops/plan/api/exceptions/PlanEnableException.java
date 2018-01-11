/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions;

/**
 * Thrown when something goes wrong with Plan initialization.
 *
 * @author Rsl1122
 */
public class PlanEnableException extends Exception {

    public PlanEnableException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlanEnableException(String message) {
        super(message);
    }
}