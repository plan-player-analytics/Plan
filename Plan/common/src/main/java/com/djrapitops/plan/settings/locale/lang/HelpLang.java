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
 * Lang for short help messages in Commands.
 *
 * @author AuroraLS3
 */
public enum HelpLang implements Lang {
    ARG_SERVER("command.argument.server.name", "CMD Arg Name - server", "server"),
    ARG_NAME_UUID("command.argument.nameOrUUID.name", "CMD Arg Name - name or uuid", "name/uuid"),
    ARG_CODE("command.argument.code.name", "CMD Arg Name - code", "${code}"),
    ARG_USERNAME("command.argument.username.name", "CMD Arg Name - username", "username"),
    ARG_GROUP("command.argument.group.name", "CMD Arg Name - group", "group"),
    ARG_FEATURE("command.argument.feature.name", "CMD Arg Name - feature", "feature"),
    ARG_SUBCOMMAND("command.argument.subcommand.name", "CMD Arg Name - subcommand", "subcommand"),
    ARG_BACKUP_FILE("command.argument.backupFile.name", "CMD Arg Name - backup-file", "backup-file"),
    ARG_EXPORT_KIND("command.argument.exportKind", "CMD Arg Name - export kind", "export kind"),
    ARG_IMPORT_KIND("command.argument.importKind", "CMD Arg Name - import kind", "import kind"),
    DESC_ARG_SERVER_IDENTIFIER("command.argument.server.description", "CMD Arg - server identifier", "Name, ID or UUID of a server"),
    DESC_ARG_PLAYER_IDENTIFIER("command.argument.nameOrUUID.description", "CMD Arg - player identifier", "Name or UUID of a player"),
    DESC_ARG_PLAYER_IDENTIFIER_REMOVE("command.argument.nameOrUUID.removeDescription", "CMD Arg - player identifier remove", "Identifier for a player that will be removed from current database."),
    DESC_ARG_CODE("command.argument.code.description", "CMD Arg - code", "Code used to finalize registration."),
    DESC_ARG_USERNAME("command.argument.username.description", "CMD Arg - username", "Username of another user. If not specified player linked user is used."),
    DESC_ARG_GROUP("command.argument.group.description", "CMD Arg - group", "Web Permission Group, case sensitive."),
    DESC_ARG_FEATURE("command.argument.feature.description", "CMD Arg - feature", "Name of the feature to disable: ${0}"),
    DESC_ARG_SUBCOMMAND("command.argument.subcommand.description", "CMD Arg - subcommand", "Use the command without subcommand to see help."),
    DESC_ARG_BACKUP_FILE("command.argument.backupFile.description", "CMD Arg - backup-file", "Name of the backup file (case sensitive)"),
    DESC_ARG_DB_BACKUP("command.argument.dbBackup.description", "CMD Arg - db type backup", "Type of the database to backup. Current database is used if not specified."),
    DESC_ARG_DB_RESTORE("command.argument.dbRestore.description", "CMD Arg - db type restore", "Type of the database to restore to. Current database is used if not specified."),
    DESC_ARG_DB_MOVE_FROM("command.argument.dbTypeMoveFrom.description", "CMD Arg - db type move from", "Type of the database to move data from."),
    DESC_ARG_DB_MOVE_TO("command.argument.dbTypeMoveTo.description", "CMD Arg - db type move to", "Type of the database to move data to. Can not be same as previous."),
    DESC_ARG_DB_HOTSWAP("command.argument.dbTypeHotswap.description", "CMD Arg - db type hotswap", "Type of the database to start using."),
    DESC_ARG_DB_REMOVE("command.argument.dbTypeRemove.description", "CMD Arg - db type clear", "Type of the database to remove all data from."),

    SERVER("command.help.server.description", "Command Help - /plan server", "View the Server Page"),
    SERVERS("command.help.servers.description", "Command Help - /plan servers", "List servers in Database"),
    NETWORK("command.help.network.description", "Command Help - /plan network", "View the Network Page"),
    PLAYER("command.help.player.description", "Command Help - /plan player", "View a Player Page"),
    PLAYERS("command.help.players.description", "Command Help - /plan players", "View the Players Page"),
    SEARCH("command.help.search.description", "Command Help - /plan search", "Search for a player name"),
    INGAME("command.help.ingame.description", "Command Help - /plan ingame", "View Player info in game"),
    REGISTER("command.help.register.description", "Command Help - /plan register", "Register a user for Plan website"),
    UNREGISTER("command.help.unregister.description", "Command Help - /plan unregister", "Unregister a user of Plan website"),
    INFO("command.help.info.description", "Command Help - /plan info", "Information about the plugin"),
    RELOAD("command.help.reload.description", "Command Help - /plan reload", "Restart Plan"),
    DISABLE("command.help.disable.description", "Command Help - /plan disable", "Disable the plugin or part of it"),
    USERS("command.help.users.description", "Command Help - /plan users", "List all web users"),
    DB("command.help.database.description", "Command Help - /plan db", "Manage Plan database"),
    DB_BACKUP("command.help.dbBackup.description", "Command Help - /plan db backup", "Backup data of a database to a file"),
    DB_RESTORE("command.help.dbRestore.description", "Command Help - /plan db restore", "Restore data from a file to a database"),
    DB_MOVE("command.help.dbMove.description", "Command Help - /plan db move", "Move data between databases"),
    DB_HOTSWAP("command.help.dbHotswap.description", "Command Help - /plan db hotswap", "Change Database quickly"),
    DB_CLEAR("command.help.dbClear.description", "Command Help - /plan db clear", "Remove ALL Plan data from a database"),
    DB_REMOVE("command.help.dbRemove.description", "Command Help - /plan db remove", "Remove player's data from Current database"),
    DB_UNINSTALLED("command.help.dbUninstalled.description", "Command Help - /plan db uninstalled", "Set a server as uninstalled in the database."),
    EXPORT("command.help.export.description", "Command Help - /plan export", "Export html or json files manually"),
    IMPORT("command.help.import.description", "Command Help - /plan import", "Import data"),
    JSON("command.help.json.description", "Command Help - /plan json", "View json of Player's raw data."),
    SET_GROUP("command.help.setgroup.description", "Command Help - /plan setgroup", "Change users web permission group."),
    GROUPS("command.help.groups.description", "Command Help - /plan groups", "List web permission groups."),
    LOGOUT("command.help.logout.description", "Command Help - /plan logout", "Log out other users from the panel."),
    JOIN_ADDRESS_REMOVAL("command.help.removejoinaddresses.description", "Command Help - /plan db removejoinaddresses", "Remove join addresses of a specified server"),
    ONLINE_UUID_MIGRATION("command.help.migrateToOnlineUuids.description", "Command Help - /plan db migratetoonlineuuids", "Migrate offline uuid data to online uuids");

    private final String identifier;
    private final String key;
    private final String defaultValue;

    HelpLang(String key, String identifier, String defaultValue) {
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