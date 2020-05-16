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
 * {@link Lang} implementation for general command language.
 *
 * @author Rsl1122
 */
public enum CommandLang implements Lang {
    FAIL_REQ_ARGS("Cmd FAIL - Requires Arguments", "§cArguments required (${0}) ${1}"),
    FAIL_REQ_ONE_ARG("Cmd FAIL - Require only one Argument", "§cSingle Argument required ${1}"),
    FAIL_NO_PERMISSION("Cmd FAIL - No Permission", "§cYou do not have the required permission."),
    FAIL_USERNAME_NOT_VALID("Cmd FAIL - Invalid Username", "§cUser does not have an UUID."),
    FAIL_USERNAME_NOT_KNOWN("Cmd FAIL - Unknown Username", "§cUser has not been seen on this server"),
    FAIL_DATABASE_NOT_OPEN("Cmd FAIL - Database not open", "§cDatabase is ${0} - Please try again a bit later."),
    WARN_DATABASE_NOT_OPEN("Cmd WARN - Database not open", "§eDatabase is ${0} - This might take longer than expected.."),
    USER_NOT_LINKED("Cmd FAIL - Users not linked", "User is not linked to your account and you don't have permission to remove other user's accounts."),

    FAIL_WEB_USER_EXISTS("Cmd FAIL - WebUser exists", "§cUser already exists!"),
    FAIL_WEB_USER_NOT_EXISTS("Cmd FAIL - WebUser does not exists", "§cUser does not exists!"),
    FAIL_NO_SUCH_FEATURE("Cmd FAIL - No Feature", "§eDefine a feature to disable! (currently supports ${0})"),

    FEATURE_DISABLED("Cmd SUCCESS - Feature disabled", "§aDisabled '${0}' temporarily until next plugin reload."),

    WEB_USER_REGISTER_SUCCESS("Cmd SUCCESS - WebUser register", "§aAdded a new user (${0}) successfully!"),
    WEB_USER_REGISTER_NOTIFY("Cmd Notify - WebUser register", "Registered new user: '${0}' Perm level: ${1}"),
    WEB_USER_LIST("Web User Listing", "  §2${0} §7: §f${1}"),
    NO_WEB_USER_NOTIFY("Cmd Notify - No WebUser", "You might not have a web user, use /plan register <password>"),
    WEB_PERMISSION_LEVELS("Cmd Web - Permission Levels", ">\\§70: Access all pages\\§71: Access '/players' and all player pages\\§72: Access player page with the same username as the webuser\\§73+: No permissions"),

    LINK_CLICK_ME("Cmd - Click Me", "Click me"),
    LINK_PREFIX("Cmd - Link", "  §2Link: §f"),

    HEADER_SEARCH("Cmd Header - Search", "> §2${0} Results for §f${1}§2:"),
    HEADER_ANALYSIS("Cmd Header - Analysis", "> §2Analysis Results"),
    HEADER_INFO("Cmd Header - Info", "> §2Player Analytics"),
    HEADER_INSPECT("Cmd Header - Inspect", "> §2Player: §f${0}"),
    HEADER_SERVERS("Cmd Header - Servers", "> §2Servers"),
    HEADER_PLAYERS("Cmd Header - Players", "> §2Players"),
    HEADER_WEB_USERS("Cmd Header - Web Users", "> §2${0} Web Users"),
    HEADER_NETWORK("Cmd Header - Network", "> §2Network Page"),

    INFO_VERSION("Cmd Info - Version", "  §2Version: §f${0}"),
    INFO_UPDATE("Cmd Info - Update", "  §2Update Available: §f${0}"),
    INFO_DATABASE("Cmd Info - Database", "  §2Active Database: §f${0}"),
    INFO_PROXY_CONNECTION("Cmd Info - Bungee Connection", "  §2Connected to Proxy: §f${0}"),

    QINSPECT_ACTIVITY_INDEX("Cmd Qinspect - Activity Index", "  §2Activity Index: §f${0} | ${1}"),
    QINSPECT_REGISTERED("Cmd Qinspect - Registered", "  §2Registered: §f${0}"),
    QINSPECT_LAST_SEEN("Cmd Qinspect - Last Seen", "  §2Last Seen: §f${0}"),
    QINSPECT_GEOLOCATION("Cmd Qinspect - Geolocation", "  §2Logged in from: §f${0}"),
    QINSPECT_PLAYTIME("Cmd Qinspect - Playtime", "  §2Playtime: §f${0}"),
    QINSPECT_ACTIVE_PLAYTIME("Cmd Qinspect - Active Playtime", "  §2Active Playtime: §f${0}"),
    QINSPECT_AFK_PLAYTIME("Cmd Qinspect - AFK Playtime", "  §2AFK Time: §f${0}"),
    QINSPECT_LONGEST_SESSION("Cmd Qinspect - Longest Session", "  §2Longest Session: §f${0}"),
    QINSPECT_TIMES_KICKED("Cmd Qinspect - Times Kicked", "  §2Times Kicked: §f${0}"),
    QINSPECT_PLAYER_KILLS("Cmd Qinspect - Player Kills", "  §2Player Kills: §f${0}"),
    QINSPECT_MOB_KILLS("Cmd Qinspect - Mob Kills", "  §2Mob Kills: §f${0}"),
    QINSPECT_DEATHS("Cmd Qinspect - Deaths", "  §2Deaths: §f${0}"),

    DISABLE_DISABLED("Cmd Disable - Disabled", "§aPlan systems are now disabled. You can still use /planbungee reload to restart the plugin."),

    RELOAD_COMPLETE("Cmd Info - Reload Complete", "§aReload Complete"),
    RELOAD_FAILED("Cmd Info - Reload Failed", "§cSomething went wrong during reload of the plugin, a restart is recommended."),
    NO_ADDRESS_NOTIFY("Cmd Notify - No Address", "§eNo address was available - using localhost as fallback. Set up 'Alternative_IP' settings.");

    private final String identifier;
    private final String defaultValue;

    CommandLang(String identifier, String defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}