/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api.exceptions;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.auth.FailReason;

/**
 * Thrown when WebUser can not be authorized (WebServer).
 *
 * @author Rsl1122
 */
public class WebUserAuthException extends WebException {

    private final FailReason failReason;

    public WebUserAuthException(FailReason failReason) {
        super(failReason.getReason());
        this.failReason = failReason;
    }

    public WebUserAuthException(FailReason failReason, String additionalInfo) {
        super(failReason.getReason() + ": " + additionalInfo);
        this.failReason = failReason;
    }

    public WebUserAuthException(Throwable cause) {
        super(FailReason.ERROR.getReason(), cause);
        this.failReason = FailReason.ERROR;
    }

    public FailReason getFailReason() {
        return failReason;
    }
}
