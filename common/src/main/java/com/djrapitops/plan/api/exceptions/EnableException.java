/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions;

/**
 * Thrown when something goes wrong with Plan initialization.
 *
 * @author Rsl1122
 */
public class EnableException extends Exception {

    public EnableException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnableException(String message) {
        super(message);
    }
}
