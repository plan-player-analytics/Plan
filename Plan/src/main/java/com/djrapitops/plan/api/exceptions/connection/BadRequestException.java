/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api.exceptions.connection;

/**
 * Thrown when connection is returned 401 Bad Request.
 *
 * @author Rsl1122
 */
public class BadRequestException extends WebException {

    public BadRequestException(String message) {
        super(message);
    }
}