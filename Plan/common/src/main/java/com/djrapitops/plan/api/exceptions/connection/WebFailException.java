/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.api.exceptions.connection;

import com.djrapitops.plan.system.webserver.response.ResponseCode;

/**
 * Group of WebExceptions that can be considered a failed connection state on some occasions.
 *
 * @author Rsl1122
 */
public class WebFailException extends WebException {

    public WebFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebFailException(String message, ResponseCode responseCode) {
        super(message, responseCode);
    }

    public WebFailException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause, responseCode);
    }

    public WebFailException(Throwable cause, ResponseCode responseCode) {
        super(cause, responseCode);
    }
}
