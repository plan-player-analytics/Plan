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
package com.djrapitops.plan.commands;

import com.djrapitops.plan.commands.subcommands.*;
import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.commands.use.CommandWithSubcommands;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.gathering.importing.ImportSystem;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Singleton
public class PlanCommand {

    private final String commandName;
    private final Locale locale;
    private final ColorScheme colors;
    private final Confirmation confirmation;
    private final ImportSystem importSystem;
    private final DBSystem dbSystem;
    private final LinkCommands linkCommands;
    private final RegistrationCommands registrationCommands;
    private final PluginStatusCommands statusCommands;
    private final DatabaseCommands databaseCommands;
    private final DataUtilityCommands dataUtilityCommands;
    private final ErrorLogger errorLogger;

    @Inject
    public PlanCommand(
            @Named("mainCommandName") String commandName,
            Locale locale,
            ColorScheme colors,
            Confirmation confirmation,
            ImportSystem importSystem,
            DBSystem dbSystem,
            LinkCommands linkCommands,
            RegistrationCommands registrationCommands,
            PluginStatusCommands statusCommands,
            DatabaseCommands databaseCommands,
            DataUtilityCommands dataUtilityCommands,
            ErrorLogger errorLogger
    ) {
        this.commandName = commandName;
        this.locale = locale;
        this.colors = colors;
        this.confirmation = confirmation;
        this.importSystem = importSystem;
        this.dbSystem = dbSystem;
        this.linkCommands = linkCommands;
        this.registrationCommands = registrationCommands;
        this.statusCommands = statusCommands;
        this.databaseCommands = databaseCommands;
        this.dataUtilityCommands = dataUtilityCommands;
        this.errorLogger = errorLogger;
    }

    private void handleException(RuntimeException error, CMDSender sender, Arguments arguments) {
        if (error instanceof IllegalArgumentException) {
            sender.send("§c" + error.getMessage());
        } else {
            errorLogger.log(L.WARN, error, ErrorContext.builder().related(sender, arguments).build());
        }
    }

    public CommandWithSubcommands build() {
        CommandWithSubcommands command = CommandWithSubcommands.builder()
                .alias(commandName)
                .colorScheme(colors)
                .subcommand(serverCommand())
                .subcommand(serversCommand())
                .subcommand(playerCommand())
                .subcommand(playersCommand())
                .subcommand(networkCommand())
                .subcommand(jsonCommand())

                .subcommand(registerCommand())
                .subcommand(unregisterCommand())
                .subcommand(webUsersCommand())

                .subcommand(acceptCommand())
                .subcommand(cancelCommand())

                .subcommand(infoCommand())
                .subcommand(reloadCommand())
                .subcommand(disableCommand())
                .subcommand(databaseCommand())

                .subcommand(exportCommand())
                .subcommand(importCommand())
                .exceptionHandler(this::handleException)
                .build();
        if (!"plan".equalsIgnoreCase(commandName)) {
            command.getAliases().add("planbungee");
            command.getAliases().add("planvelocity");
            command.getAliases().add("planproxy");
        }
        return command;
    }

    public List<String> serverNames(CMDSender sender, Arguments arguments) {
        String asString = arguments.concatenate(" ");
        List<Server> servers = dbSystem.getDatabase().query(ServerQueries.findMatchingServers(asString));
        return Lists.map(servers, Server::getIdentifiableName);
    }

    private List<String> playerNames(CMDSender sender, Arguments arguments) {
        String asString = arguments.concatenate(" ");
        return dbSystem.getDatabase().query(UserIdentifierQueries.fetchMatchingPlayerNames(asString));
    }

    private Subcommand serverCommand() {
        return Subcommand.builder()
                .aliases("server", "analyze", "a", "analyse", "analysis")
                .optionalArgument("server", "Name, ID or UUID of a server")
                .requirePermission("plan.server")
                .description("View a server page")
                .inDepthDescription("Obtain a link to the /server page of a specific server, or the current server if no arguments are given.")
                .onTabComplete(this::serverNames)
                .onCommand(linkCommands::onServerCommand)
                .build();
    }

    private Subcommand serversCommand() {
        return Subcommand.builder()
                .aliases("servers", "serverlist", "listservers", "sl", "ls")
                .requirePermission("plan.servers")
                .description("List servers")
                .inDepthDescription("List ids, names and uuids of servers in the database.")
                .onCommand(linkCommands::onServersCommand)
                .build();
    }

    private Subcommand playerCommand() {
        return Subcommand.builder()
                .aliases("player", "inspect")
                .optionalArgument("name/uuid", "Name or UUID of a player")
                .requirePermission("plan.player.self")
                .description("View a player page")
                .inDepthDescription("Obtain a link to the /player page of a specific player, or the current player.")
                .onTabComplete(this::playerNames)
                .onCommand(linkCommands::onPlayerCommand)
                .build();
    }

    private Subcommand playersCommand() {
        return Subcommand.builder()
                .aliases("players", "pl", "playerlist", "list")
                .requirePermission("plan.player.other")
                .description("View players page")
                .inDepthDescription("Obtain a link to the /players page to see a list of players.")
                .onCommand(linkCommands::onPlayersCommand)
                .build();
    }

    private Subcommand networkCommand() {
        return Subcommand.builder()
                .aliases("network", "netw")
                .requirePermission("plan.network")
                .description("View network page")
                .inDepthDescription("Obtain a link to the /network page, only does so on networks.")
                .onCommand(linkCommands::onNetworkCommand)
                .build();
    }

    private Subcommand registerCommand() {
        return Subcommand.builder()
                .aliases("register")
                .requirePermission("plan.register.self")
                .optionalArgument("--code ${code}", "Code used to finalize registration.")
                .description("Register a user for Plan website")
                .inDepthDescription("Use without arguments to get link to register page. Use --code [code] after registration to get a user.")
                .onCommand(registrationCommands::onRegister)
                .onTabComplete((sender, arguments) -> arguments.isEmpty() ? Collections.singletonList("--code") : Collections.emptyList())
                .build();
    }

    private Subcommand unregisterCommand() {
        return Subcommand.builder()
                .aliases("unregister")
                .requirePermission("plan.unregister.self")
                .optionalArgument("username", "Username of another user. If not specified linked user is used.")
                .description("Unregister user of Plan website")
                .inDepthDescription("Use without arguments to unregister linked user, or with username argument to unregister another user.")
                .onCommand((sender, arguments) -> registrationCommands.onUnregister(commandName, sender, arguments))
                .build();
    }

    private Subcommand acceptCommand() {
        return Subcommand.builder()
                .aliases("accept", "yes", "y")
                .onCommand(confirmation::onAcceptCommand)
                .build();
    }

    private Subcommand cancelCommand() {
        return Subcommand.builder()
                .aliases("cancel", "deny", "no", "n")
                .onCommand(confirmation::onCancelCommand)
                .build();
    }

    private Subcommand infoCommand() {
        return Subcommand.builder()
                .aliases("info")
                .requirePermission("plan.info")
                .description("Information about the plugin")
                .inDepthDescription("Display the current status of the plugin.")
                .onCommand(statusCommands::onInfo)
                .build();
    }

    private Subcommand reloadCommand() {
        return Subcommand.builder()
                .aliases("reload")
                .requirePermission("plan.reload")
                .description("Reload the plugin")
                .inDepthDescription("Disable and enable the plugin to reload any changes in config.")
                .onCommand(statusCommands::onReload)
                .build();
    }

    private Subcommand disableCommand() {
        return Subcommand.builder()
                .aliases("disable")
                .requirePermission("plan.disable")
                .optionalArgument("feature", "Name of the feature to disable: kickcount")
                .description("Disable the plugin or part of it")
                .inDepthDescription("Disable the plugin or part of it until next reload/restart.")
                .onCommand(statusCommands::onDisable)
                .build();
    }

    private Subcommand webUsersCommand() {
        return Subcommand.builder()
                .aliases("webusers", "users")
                .requirePermission("plan.register.other")
                .description("List all web users")
                .inDepthDescription("Lists web users as a table.")
                .onCommand(linkCommands::onWebUsersCommand)
                .build();
    }

    private Subcommand databaseCommand() {
        return CommandWithSubcommands.builder()
                .aliases("db", "database")
                .requirePermission("plan.data.base")
                .optionalArgument("subcommand", "Use the command without subcommand to see help.")
                .description("Manage Plan database")
                .colorScheme(colors)
                .subcommand(backupCommand())
                .subcommand(restoreCommand())
                .subcommand(moveCommand())
                .subcommand(hotswapCommand())
                .subcommand(clearCommand())
                .subcommand(removeCommand())
                .subcommand(uninstalledCommand())
                .inDepthDescription("Use different database subcommands to change the data in some way")
                .build();
    }

    private Subcommand backupCommand() {
        return Subcommand.builder()
                .aliases("backup")
                .requirePermission("plan.data.backup")
                .optionalArgument("MySQL/SQlite/H2", "Type of the database to backup. Current database is used if not specified.")
                .description("Backup data of a database to a file")
                .inDepthDescription("Uses SQLite to backup one of the usable databases to a file.")
                .onCommand(databaseCommands::onBackup)
                .build();
    }

    private Subcommand restoreCommand() {
        return Subcommand.builder()
                .aliases("restore")
                .requirePermission("plan.data.restore")
                .requiredArgument("backup-file", "Name of the backup file (case sensitive)")
                .optionalArgument("MySQL/SQlite/H2", "Type of the database to restore to. Current database is used if not specified.")
                .description("Restore data from a file to a database")
                .inDepthDescription("Uses SQLite to backup file and overwrites contents of the target database.")
                .onCommand((sender, arguments) -> databaseCommands.onRestore(commandName, sender, arguments))
                .build();
    }

    private Subcommand moveCommand() {
        return Subcommand.builder()
                .aliases("move")
                .requirePermission("plan.data.move")
                .requiredArgument("MySQL/SQlite/H2", "Type of the database to move data from.")
                .requiredArgument("MySQL/SQlite/H2", "Type of the database to move data to. Can not be same as previous.")
                .description("Move data between databases")
                .inDepthDescription("Overwrites contents in the other database with the contents in another.")
                .onCommand((sender, arguments) -> databaseCommands.onMove(commandName, sender, arguments))
                .build();
    }

    private Subcommand hotswapCommand() {
        return Subcommand.builder()
                .aliases("hotswap")
                .requirePermission("plan.data.hotswap")
                .requiredArgument("MySQL/SQlite/H2", "Type of the database to start using.")
                .description("Move data between databases")
                .inDepthDescription("Reloads the plugin with the other database and changes the config to match.")
                .onCommand(databaseCommands::onHotswap)
                .build();
    }

    private Subcommand clearCommand() {
        return Subcommand.builder()
                .aliases("clear")
                .requirePermission("plan.data.clear")
                .requiredArgument("MySQL/SQlite/H2", "Type of the database to remove data from.")
                .description("Remove ALL Plan data from a database")
                .inDepthDescription("Clears all Plan tables, removing all Plan-data in the process.")
                .onCommand((sender, arguments) -> databaseCommands.onClear(commandName, sender, arguments))
                .build();
    }

    private Subcommand removeCommand() {
        return Subcommand.builder()
                .aliases("remove")
                .requirePermission("plan.data.remove")
                .requiredArgument("name/uuid", "Identifier for a player that will be removed from current database.")
                .description("Remove player's data from Current database.")
                .inDepthDescription("Removes all data linked to a player from the Current database.")
                .onCommand((sender, arguments) -> databaseCommands.onRemove(commandName, sender, arguments))
                .build();
    }

    private Subcommand uninstalledCommand() {
        return Subcommand.builder()
                .aliases("uninstalled")
                .requirePermission("plan.data.uninstalled")
                .requiredArgument("server", "Name, ID or UUID of a server")
                .description("Set a server as uninstalled in the database.")
                .inDepthDescription("Marks a server in Plan database as uninstalled so that it will not show up in server queries.")
                .onCommand(databaseCommands::onUninstalled)
                .build();
    }

    private Subcommand exportCommand() {
        return Subcommand.builder()
                .aliases("export")
                .requirePermission("plan.data.export")
                .optionalArgument("export kind", "players/server_json")
                .description("Export html or json files manually.")
                .inDepthDescription("Performs an export to export location defined in the config.")
                .onCommand(dataUtilityCommands::onExport)
                .build();
    }

    private Subcommand importCommand() {
        List<String> importerNames = importSystem.getImporterNames();
        if (importerNames.isEmpty()) return null;
        return Subcommand.builder()
                .aliases("import")
                .requirePermission("plan.data.import")
                .optionalArgument("import kind", importerNames.toString())
                .description("Import data.")
                .inDepthDescription("Performs an import to load data into the database.")
                .onCommand(dataUtilityCommands::onImport)
                .build();
    }

    private Subcommand jsonCommand() {
        return Subcommand.builder()
                .aliases("json")
                .requirePermission("plan.json.self")
                .requiredArgument("name/uuid", "Name or UUID of a player")
                .description("View json of Player's raw data.")
                .inDepthDescription("Allows you to download a player's data in json format. All of it.")
                .onCommand(linkCommands::onJson)
                .build();
    }
}