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
package com.djrapitops.plan.exceptions.connection;

import com.djrapitops.plan.delivery.webserver.response.ResponseCode;

/**
 * Thrown when Connection POST-request fails, general Exception.
 *
 * @author Rsl1122
 */
public class WebException extends Exception {

    private final ResponseCode responseCode;

    public WebException() {
        responseCode = ResponseCode.NONE;
    }

    public WebException(String message) {
        super(message);
        responseCode = ResponseCode.NONE;
    }

    public WebException(String message, Throwable cause) {
        super(message, cause);
        responseCode = ResponseCode.NONE;
    }

    public WebException(Throwable cause) {
        super(cause);
        responseCode = ResponseCode.NONE;
    }

    public WebException(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public WebException(String message, ResponseCode responseCode) {
        super(message);
        this.responseCode = responseCode;
    }

    public WebException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    public WebException(Throwable cause, ResponseCode responseCode) {
        super(cause);
        this.responseCode = responseCode;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }
}
