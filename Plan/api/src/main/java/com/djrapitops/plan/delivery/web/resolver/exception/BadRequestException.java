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
package com.djrapitops.plan.delivery.web.resolver.exception;

/**
 * Throw this exception when a Resolver gets invalid query for JSON.
 * <p>
 * Plan will construct bad request error json automatically.
 * Note that you might need to handle the error page, which is json: {@code {"status": 400, "error": "message"}}
 *
 * @author AuroraLS3
 */
public class BadRequestException extends IllegalArgumentException {

    /**
     * Default constructor.
     *
     * @param message Error message - avoid including any input incoming in the request to prevent XSS.
     */
    public BadRequestException(String message) {
        super(message);
    }
}
