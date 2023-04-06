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
 * @author AuroraLS3
 */
public enum CommandLang implements Lang {
    CONFIRM_EXPIRED("command.confirmation.expired", "Cmd Confirm - Expired", "Confirmation expired, use the command again"),
    CONFIRM_FAIL_ACCEPT("command.fail.onAccept", "Cmd Confirm - Fail on accept", "The accepted action errored upon execution: ${0}"),
    CONFIRM_FAIL_DENY("command.fail.onDeny", "Cmd Confirm - Fail on deny", "The denied action errored upon execution: ${0}"),
    CONFIRM("command.confirmation.confirm", "Cmd Confirm - confirmation", "Confirm: "),
    CONFIRM_ACCEPT("command.confirmation.accept", "Cmd Confirm - accept", "Accept"),
    CONFIRM_DENY("command.confirmation.deny", "Cmd Confirm - deny", "Cancel"),
    CONFIRM_OVERWRITE_DB("command.confirmation.dbOverwrite", "Cmd Confirm - overwriting db", "You are about to overwrite data in Plan ${0} with data in ${1}"),
    CONFIRM_CLEAR_DB("command.confirmation.dbClear", "Cmd Confirm - clearing db", "You are about to remove all Plan-data in ${0}"),
    CONFIRM_REMOVE_PLAYER_DB("command.confirmation.dbRemovePlayer", "Cmd Confirm - remove player db", "You are about to remove data of ${0} from ${1}"),
    CONFIRM_UNREGISTER("command.confirmation.unregister", "Cmd Confirm - unregister", "You are about to unregister '${0}' linked to ${1}"),
    CONFIRM_CANCELLED_DATA("command.confirmation.cancelNoChanges", "Cmd Confirm - cancelled, no data change", "Cancelled. No data was changed."),
    CONFIRM_CANCELLED_UNREGISTER("command.confirmation.cancelNoUnregister", "Cmd Confirm - cancelled, unregister", "Cancelled. '${0}' was not unregistered"),

    FAIL_PLAYER_NOT_FOUND("command.fail.playerNotFound", "Cmd FAIL - No player", "Player '${0}' was not found, they have no UUID."),
    FAIL_PLAYER_NOT_FOUND_REGISTER("command.fail.playerNotInDatabase", "Cmd FAIL - No player register", "Player '${0}' was not found in the database."),
    FAIL_SERVER_NOT_FOUND("command.fail.serverNotFound", "Cmd FAIL - No server", "Server '${0}' was not found from the database."),
    FAIL_EMPTY_SEARCH_STRING("command.fail.emptyString", "Cmd FAIL - Empty search string", "The search string can not be empty"),
    FAIL_ACCEPTS_ARGUMENTS("command.fail.invalidArguments", "Cmd FAIL - Accepts only these arguments", "Accepts following as ${0}: ${1}"),
    FAIL_REQ_ARGS("command.fail.missingArguments", "Cmd FAIL - Requires Arguments", "§cArguments required (${0}) ${1}"),
    FAIL_REQ_ONE_ARG("command.fail.tooManyArguments", "Cmd FAIL - Require only one Argument", "§cSingle Argument required ${1}"),
    FAIL_NO_PERMISSION("command.fail.noPermission", "Cmd FAIL - No Permission", "§cYou do not have the required permission."),
    FAIL_USERNAME_NOT_VALID("command.fail.invalidUsername", "Cmd FAIL - Invalid Username", "§cUser does not have an UUID."),
    FAIL_USERNAME_NOT_KNOWN("command.fail.unknownUsername", "Cmd FAIL - Unknown Username", "§cUser has not been seen on this server"),
    FAIL_DATABASE_NOT_OPEN("command.database.failDbNotOpen", "Cmd FAIL - Database not open", "§cDatabase is ${0} - Please try again a bit later."),
    WARN_DATABASE_NOT_OPEN("command.database.warnDbNotOpen", "Cmd WARN - Database not open", "§eDatabase is ${0} - This might take longer than expected.."),
    USER_NOT_LINKED("command.fail.missingLink", "Cmd FAIL - Users not linked", "User is not linked to your account and you don't have permission to remove other user's accounts."),

    FAIL_WEB_USER_EXISTS("command.fail.webUserExists", "Cmd FAIL - WebUser exists", "§cUser already exists!"),
    FAIL_WEB_USER_NOT_EXISTS("command.fail.webUserNotFound", "Cmd FAIL - WebUser does not exists", "§cUser does not exists!"),
    FAIL_NO_SUCH_FEATURE("command.fail.missingFeature", "Cmd FAIL - No Feature", "§eDefine a feature to disable! (currently supports ${0})"),
    FAIL_SEE_CONFIG_SETTING("command.fail.seeConfig", "Cmd FAIL - see config", "see '${0}' in config.yml"),

    FEATURE_DISABLED("command.general.featureDisabled", "Cmd SUCCESS - Feature disabled", "§aDisabled '${0}' temporarily until next plugin reload."),

    WEB_USER_REGISTER_SUCCESS("command.general.successWebUserRegister", "Cmd SUCCESS - WebUser register", "§aAdded a new user (${0}) successfully!"),
    WEB_USER_REGISTER_NOTIFY("command.general.notifyWebUserRegister", "Cmd Notify - WebUser register", "Registered new user: '${0}' Perm level: ${1}"),
    WEB_USER_LIST("command.general.webUserList", "Web User Listing", "  §2${0} §7: §f${1}"),
    NO_WEB_USER_NOTIFY("command.general.noWebuser", "Cmd Notify - No WebUser", "You might not have a web user, use /plan register <password>"),
    WEB_PERMISSION_LEVELS("command.general.webPermissionLevels", "Cmd Web - Permission Levels", ">\\§70: Access all pages\\§71: Access '/players' and all player pages\\§72: Access player page with the same username as the webuser\\§73+: No permissions"),

    LINK_CLICK_ME("command.link.clickMe", "Cmd - Click Me", "Click me"),
    LINK("command.link.link", "Cmd - Link", "Link"),
    LINK_SERVER("command.link.server", "Cmd - Link Server", "Server page: "),
    LINK_PLAYER("command.link.player", "Cmd - Link Player", "Player page: "),
    LINK_PLAYERS("command.link.players", "Cmd - Link Players", "Players page: "),
    LINK_NETWORK("command.link.network", "Cmd - Link Network", "Network page: "),
    LINK_JSON("command.link.playerJson", "Cmd - Link Player JSON", "Player json: "),
    LINK_REGISTER("command.link.register", "Cmd - Link Register", "Register page: "),

    HEADER_HELP("command.header.help", "Cmd Header - Help", "> §2/${0} Help"),
    FOOTER_HELP("command.footer.help", "Cmd Footer - Help", "§7Hover over command or arguments or use '/${0} ?' to learn more about them."),
    HEADER_SEARCH("command.header.search", "Cmd Header - Search", "> §2${0} Results for §f${1}§2:"),
    HEADER_ANALYSIS("command.header.analysis", "Cmd Header - Analysis", "> §2Analysis Results"),
    HEADER_INFO("command.header.info", "Cmd Header - Info", "> §2Player Analytics"),
    HEADER_INSPECT("command.header.inspect", "Cmd Header - Inspect", "> §2Player: §f${0}"),
    HEADER_SERVERS("command.header.servers", "Cmd Header - Servers", "> §2Servers"),
    HEADER_PLAYERS("command.header.players", "Cmd Header - Players", "> §2Players"),
    HEADER_WEB_USERS("command.header.webUsers", "Cmd Header - Web Users", "> §2${0} Web Users"),
    HEADER_NETWORK("command.header.network", "Cmd Header - Network", "> §2Network Page"),
    HEADER_SERVER_LIST("command.header.serverList", "Cmd Header - server list", "id::name::uuid::version"),
    HEADER_WEB_USER_LIST("command.header.webUserList", "Cmd Header - web user list", "username::linked to::permission level"),

    INFO_VERSION("command.subcommand.info.version", "Cmd Info - Version", "  §2Version: §f${0}"),
    INFO_UPDATE("command.subcommand.info.update", "Cmd Info - Update", "  §2Update Available: §f${0}"),
    INFO_DATABASE("command.subcommand.info.database", "Cmd Info - Database", "  §2Current Database: §f${0}"),
    INFO_PROXY_CONNECTION("command.subcommand.info.proxy", "Cmd Info - Bungee Connection", "  §2Connected to Proxy: §f${0}"),
    INFO_SERVER_UUID("command.subcommand.info.serverUUID", "Cmd Info - Server UUID", "  §2Server UUID: §f${0}"),

    INGAME_ACTIVITY_INDEX("command.ingame.activityIndex", "Cmd Qinspect - Activity Index", "  §2Activity Index: §f${0} | ${1}"),
    INGAME_REGISTERED("command.ingame.registered", "Cmd Qinspect - Registered", "  §2Registered: §f${0}"),
    INGAME_LAST_SEEN("command.ingame.lastSeen", "Cmd Qinspect - Last Seen", "  §2Last Seen: §f${0}"),
    INGAME_GEOLOCATION("command.ingame.geolocation", "Cmd Qinspect - Geolocation", "  §2Logged in from: §f${0}"),
    INGAME_PLAYTIME("command.ingame.playtime", "Cmd Qinspect - Playtime", "  §2Playtime: §f${0}"),
    INGAME_ACTIVE_PLAYTIME("command.ingame.activePlaytime", "Cmd Qinspect - Active Playtime", "  §2Active Playtime: §f${0}"),
    INGAME_AFK_PLAYTIME("command.ingame.afkPlaytime", "Cmd Qinspect - AFK Playtime", "  §2AFK Time: §f${0}"),
    INGAME_LONGEST_SESSION("command.ingame.longestSession", "Cmd Qinspect - Longest Session", "  §2Longest Session: §f${0}"),
    INGAME_TIMES_KICKED("command.ingame.timesKicked", "Cmd Qinspect - Times Kicked", "  §2Times Kicked: §f${0}"),
    INGAME_PLAYER_KILLS("command.ingame.playerKills", "Cmd Qinspect - Player Kills", "  §2Player Kills: §f${0}"),
    INGAME_MOB_KILLS("command.ingame.mobKills", "Cmd Qinspect - Mob Kills", "  §2Mob Kills: §f${0}"),
    INGAME_DEATHS("command.ingame.deaths", "Cmd Qinspect - Deaths", "  §2Deaths: §f${0}"),

    DB_BACKUP_CREATE("command.database.creatingBackup", "Cmd db - creating backup", "Creating a backup file '${0}.db' with contents of ${1}"),
    DB_WRITE("command.database.write", "Cmd db - write", "Writing to ${0}.."),
    DB_REMOVAL("command.database.removal", "Cmd db - removal", "Removing Plan-data from ${0}.."),
    DB_REMOVAL_PLAYER("command.database.playerRemoval", "Cmd db - removal player", "Removing data of ${0} from ${1}.."),
    DB_UNINSTALLED("command.database.serverUninstalled", "Cmd db - server uninstalled", "§aIf the server is still installed, it will automatically set itself as installed in the database."),
    UNREGISTER("command.database.unregister", "Cmd unregister - unregistering", "Unregistering '${0}'.."),

    DISABLE_DISABLED("command.general.pluginDisabled", "Cmd Disable - Disabled", "§aPlan systems are now disabled. You can still use reload to restart the plugin."),

    NOTIFY_NO_NETWORK("command.link.noNetwork", "Cmd network - No network", "Server is not connected to a network. The link redirects to server page."),
    RELOAD_COMPLETE("command.general.reloadComplete", "Cmd Info - Reload Complete", "§aReload Complete"),
    RELOAD_FAILED("command.general.reloadFailed", "Cmd Info - Reload Failed", "§cSomething went wrong during reload of the plugin, a restart is recommended."),
    NO_ADDRESS_NOTIFY("command.general.noAddress", "Cmd Notify - No Address", "§eNo address was available - using localhost as fallback. Set up 'Alternative_IP' settings."),
    HOTSWAP_REMINDER("command.database.manage.hotswap", "Manage - Remind HotSwap", "§eRemember to swap to the new database (/plan db hotswap ${0}) & reload the plugin."),
    PROGRESS_START("command.database.manage.start", "Manage - Start", "> §2Processing data.."),
    PROGRESS("command.database.manage.progress", "Manage - Progress", "${0} / ${1} processed.."),
    PROGRESS_PREPARING("command.database.manage.preparing", "Manage - preparing", "Preparing.."),
    PROGRESS_SUCCESS("command.database.manage.success", "Manage - Success", "> §aSuccess!"),
    PROGRESS_FAIL("command.database.manage.fail", "Manage - Fail", "> §cSomething went wrong: ${0}"),
    CONFIRMATION("command.database.manage.confirm", "Manage - Fail, Confirmation", "> §cAdd '-a' argument to confirm execution: ${0}"),
    IMPORTERS("command.database.manage.importers", "Manage - List Importers", "Importers: "),
    CONFIRM_OVERWRITE("command.database.manage.confirmOverwrite", "Manage - Confirm Overwrite", "Data in ${0} will be overwritten!"),
    CONFIRM_REMOVAL("command.database.manage.confirmRemoval", "Manage - Confirm Removal", "Data in ${0} will be removed!"),
    CONFIRM_JOIN_ADDRESS_REMOVAL("command.database.manage.confirmPartialRemoval", "Manage - Confirm Partial Removal", "Join Address Data for Server ${0} in ${1} will be removed!"),
    FAIL_SAME_DB("command.database.manage.failSameDB", "Manage - Fail Same Database", "> §cCan not operate on to and from the same database!"),
    FAIL_INCORRECT_DB("command.database.manage.failIncorrectDB", "Manage - Fail Incorrect Database", "> §c'${0}' is not a supported database."),
    FAIL_FILE_NOT_FOUND("command.database.manage.failFileNotFound", "Manage - Fail File not found", "> §cNo File found at ${0}"),
    FAIL_IMPORTER_NOT_FOUND("command.general.failNoImporter", "Manage - Fail No Importer", "§eImporter '${0}' doesn't exist"),
    FAIL_EXPORTER_NOT_FOUND("command.general.failNoExporter", "Manage - Fail No Exporter", "§eExporter '${0}' doesn't exist"),
    NO_SERVER("command.database.manage.failNoServer", "Manage - Fail No Server", "No server found with given parameters."),
    UNINSTALLING_SAME_SERVER("command.database.manage.failSameServer", "Manage - Fail Same server", "Can not mark this server as uninstalled (You are on it)"),
    ;

    private final String key;
    private final String identifier;
    private final String defaultValue;

    CommandLang(String key, String identifier, String defaultValue) {
        this.key = key;
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getKey() {return key;}

    @Override
    public String getDefault() {
        return defaultValue;
    }
}