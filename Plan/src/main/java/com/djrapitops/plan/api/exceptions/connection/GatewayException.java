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
 * Thrown when InfoRequest can not connect to the other server.
 *
 * @author Rsl1122
 */
public class GatewayException extends WebException {

    public GatewayException(String message) {
        super(message, ResponseCode.GATEWAY_ERROR);
    }
}
