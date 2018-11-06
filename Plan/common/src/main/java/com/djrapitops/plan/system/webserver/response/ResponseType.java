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
package com.djrapitops.plan.system.webserver.response;

/**
 * Enum for HTTP content-type response header Strings.
 *
 * @author Rsl1122
 */
public enum ResponseType {
    HTML("text/html; charset=utf-8"),
    CSS("text/css"),
    JSON("application/json"),
    JAVASCRIPT("application/javascript"),
    IMAGE("image/gif"),
    X_ICON("image/x-icon");

    private final String type;

    ResponseType(String type) {
        this.type = type;
    }

    public String get() {
        return type;
    }
}
