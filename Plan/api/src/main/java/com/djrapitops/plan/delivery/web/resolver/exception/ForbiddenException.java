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
 * Throw this exception when someone is not allowed to view a resource.
 * <p>
 * Plan will construct forbidden request error automatically.
 *
 * @author AuroraLS3
 */
public class ForbiddenException extends IllegalArgumentException {

    private final boolean json;

    public ForbiddenException(String message, boolean json) {
        super(message);
        this.json = json;
    }

    /**
     * Construct new ForbiddenException.
     *
     * @param json If the error body should be json instead of html.
     */
    public ForbiddenException(boolean json) {
        this.json = json;
    }

    public boolean isJson() {
        return json;
    }
}
