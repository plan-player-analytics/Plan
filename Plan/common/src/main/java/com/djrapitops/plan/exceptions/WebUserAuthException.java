/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.exceptions;

import com.djrapitops.plan.delivery.webserver.auth.FailReason;

/**
 * Thrown when WebUser can not be authorized (WebServer).
 *
 * @author AuroraLS3
 */
public class WebUserAuthException extends IllegalStateException {

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
