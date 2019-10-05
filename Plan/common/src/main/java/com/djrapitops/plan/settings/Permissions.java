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
package com.djrapitops.plan.settings;

/**
 * Permissions class is used easily check every permission node.
 *
 * @author Rsl1122
 */
public enum Permissions {

    HELP("plan.?"),

    INSPECT("plan.inspect.base"),
    QUICK_INSPECT("plan.qinspect.base"),
    INSPECT_OTHER("plan.inspect.other"),
    QUICK_INSPECT_OTHER("plan.qinspect.other"),

    ANALYZE("plan.analyze"),

    SEARCH("plan.search"),

    RELOAD("plan.reload"),
    INFO("plan.info"),
    MANAGE("plan.manage"),
    MANAGE_WEB("plan.webmanage"),

    IGNORE_COMMAND_USE("plan.ignore.commanduse"),
    IGNORE_AFK("plan.ignore.afk");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    /**
     * Returns the permission node in plugin.yml.
     *
     * @return permission node eg. plan.inspect.base
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Same as {@link #getPermission()}.
     *
     * @return permission node eg. plan.inspect.base
     */
    public String getPerm() {
        return getPermission();
    }
}
