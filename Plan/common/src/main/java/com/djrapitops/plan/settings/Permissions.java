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
 * @author AuroraLS3
 */
public enum Permissions {
    USE_COMMAND("plan.command"),
    SERVER("plan.server"),
    SERVERS("plan.servers"),
    NETWORK("plan.network"),
    PLAYER_SELF("plan.player.self"),
    PLAYER_OTHER("plan.player.other"),
    SEARCH("plan.search"),
    INGAME_SELF("plan.ingame.self"),
    INGAME_OTHER("plan.ingame.other"),
    REGISTER_SELF("plan.register.self"),
    REGISTER_OTHER("plan.register.other"),
    UNREGISTER_SELF("plan.unregister.self"),
    UNREGISTER_OTHER("plan.unregister.other"),
    SET_GROUP("plan.setgroup.other"),
    LOGOUT_OTHER("plan.logout.other"),
    INFO("plan.info"),
    RELOAD("plan.reload"),
    DISABLE("plan.disable"),
    USERS("plan.users"),

    DATA_BASE("plan.data.base"),
    DATA_BACKUP("plan.data.backup"),
    DATA_RESTORE("plan.data.restore"),
    DATA_MOVE("plan.data.move"),
    DATA_HOTSWAP("plan.data.hotswap"),
    DATA_CLEAR("plan.data.clear"),
    DATA_REMOVE_PLAYER("plan.data.remove.player"),
    DATA_REMOVE_SERVER("plan.data.remove.server"),
    DATA_EXPORT("plan.data.export"),
    DATA_IMPORT("plan.data.import"),

    JSON_SELF("plan.json.self"),
    JSON_OTHER("plan.json.other"),

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
        return permission;
    }

    /**
     * Returns the permission node in plugin.yml.
     *
     * @return permission node eg. plan.inspect.base
     */
    public String get() {
        return permission;
    }
}
