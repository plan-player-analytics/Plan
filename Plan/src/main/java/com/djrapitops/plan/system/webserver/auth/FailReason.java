/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.auth;

/**
 * Reason for WebUserAuthException.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.api.exceptions.WebUserAuthException
 */
public enum FailReason {
    USER_AND_PASS_NOT_SPECIFIED("User and Password not specified"),
    USER_DOES_NOT_EXIST("User does not exist"),
    USER_PASS_MISMATCH("User and Password did not match"),
    ERROR("Authentication failed due to error");

    private final String reason;

    FailReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
