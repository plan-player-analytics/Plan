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
package com.djrapitops.plan.delivery.webserver.auth;

import com.djrapitops.plan.settings.locale.lang.Lang;

/**
 * Reason for WebUserAuthException.
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.exceptions.WebUserAuthException
 */
public enum FailReason implements Lang {
    NO_USER_PRESENT("html.error.auth.noCookie", "User cookie not present"),
    EXPIRED_COOKIE("html.error.auth.expiredCookie", "User cookie has expired"),
    USER_AND_PASS_NOT_SPECIFIED("html.error.auth.emptyForm", "User and Password not specified"),
    USER_DOES_NOT_EXIST("html.error.auth.userNotFound", "User does not exist"),
    GROUP_DOES_NOT_EXIST("html.error.auth.groupNotFound", "Web Permission Group does not exist"),
    USER_INFORMATION_NOT_FOUND("html.error.auth.registrationFailed", "Registration failed, try again (The code expires after 15 minutes)"),
    NO_PERMISSION_GROUP("html.error.auth.noPermissionGroup", "Registration failed, player did not have any 'plan.webgroup.{name}' permission"),
    USER_PASS_MISMATCH("html.error.auth.loginFailed", "User and Password did not match"),
    DATABASE_NOT_OPEN("html.error.auth.dbClosed", "Database is not open, check db status with /plan info"),
    ERROR("html.error.auth.generic", "Authentication failed due to error");

    private final String key;
    private final String reason;

    FailReason(String key, String reason) {
        this.key = key;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getIdentifier() {
        return "HTML - " + name();
    }

    @Override
    public String getKey() {return key;}

    @Override
    public String getDefault() {
        return getReason();
    }
}
