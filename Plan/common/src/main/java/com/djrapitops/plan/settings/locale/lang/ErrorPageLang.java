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
package com.djrapitops.plan.settings.locale.lang;

/**
 * {@link Lang} implementation for all error pages.
 *
 * @author AuroraLS3
 */
public enum ErrorPageLang implements Lang {
    UUID_404("html.error.UUIDNotFound", "Player UUID was not found in the database."),
    NO_SERVERS_404("html.error.noServersOnline", "No Servers online to perform the request."),
    NOT_PLAYED_404("html.error.playerNotSeen", "Plan has not seen this player."),
    UNKNOWN_PAGE_404("html.error.404UnknownPage", "Make sure you're accessing a link given by a command, Examples:</p><p>/player/{uuid/name}<br>/server/{uuid/name/id}</p>"),
    UNAUTHORIZED_401("html.error.401Unauthorized", "Unauthorized"),
    AUTHENTICATION_FAILED_401("html.error.authFailed", "Authentication Failed."),
    AUTH_FAIL_TIPS_401("html.error.authFailedTips", "- Ensure you have registered a user with <b>/plan register</b><br>- Check that the username and password are correct<br>- Username and password are case-sensitive<br><br>If you have forgotten your password, ask a staff member to delete your old user and re-register."),
    FORBIDDEN_403("html.error.403Forbidden", "Forbidden"),
    ACCESS_DENIED_403("403AccessDenied", "Access Denied"),
    NOT_FOUND_404("html.error.404NotFound", "Not Found"),
    NO_SUCH_SERVER_404("html.error.serverNotSeen", "Server doesn't exist"),
    NO_SUCH_SERVER_404_EXPORT("html.error.serverNotExported", "Server doesn't exist, its data might not have been exported yet."),
    PAGE_NOT_FOUND_404("html.error.404PageNotFound", "Page does not exist.");

    private final String key;
    private final String defaultValue;

    ErrorPageLang(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML ERRORS - " + name();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}
