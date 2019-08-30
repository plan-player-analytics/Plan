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
 * {@link Lang} implementation for commonly used .html replacement values.
 *
 * @author Rsl1122
 */
public enum CommonHtmlLang implements Lang {
    PLEASE_WAIT("Please wait..."),

    NAV_INFORMATION("Information"),
    NAV_SESSIONS("Sessions"),
    NAV_OVERVIEW("Overview"),
    NAV_PLUGINS("Plugins"),
    NAV_ONLINE_ACTIVITY("Online Activity"),
    NAV_SEVER_HEALTH("Server Health"),
    NAV_PERFORMANCE("Performance"),
    NAV_PLAYERS("Players"),
    NAV_GEOLOCATIONS("Geolocations"),
    NAV_COMMAND_USAGE("Command Usage"),
    NAV_NETWORK_PLAYERS("Network Players"),

    AVERAGE_PING("Average Ping"),
    BEST_PING("Best Ping"),
    WORST_PING("Worst Ping"),
    PLAYERS_ONLINE_TEXT("Players Online");

    private final String defaultValue;

    CommonHtmlLang(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML - " + name();
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}