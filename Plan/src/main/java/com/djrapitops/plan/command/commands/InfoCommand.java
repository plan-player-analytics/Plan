package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.GenericLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;

import javax.inject.Inject;

/**
 * This SubCommand is used to view the version and the database type in use.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class InfoCommand extends CommandNode {

    private final PlanPlugin plugin;
    private final Locale locale;
    private final Database database;
    private final ConnectionSystem connectionSystem;
    private final VersionCheckSystem versionCheckSystem;

    @Inject
    public InfoCommand(
            PlanPlugin plugin,
            Locale locale,
            Database database,
            ConnectionSystem connectionSystem,
            VersionCheckSystem versionCheckSystem
    ) {
        super("info", Permissions.INFO.getPermission(), CommandType.CONSOLE);

        this.plugin = plugin;
        this.locale = locale;
        this.database = database;
        this.connectionSystem = connectionSystem;
        this.versionCheckSystem = versionCheckSystem;

        setShortHelp(locale.get(CmdHelpLang.INFO).toString());
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        String yes = locale.getString(GenericLang.YES);
        String no = locale.getString(GenericLang.NO);

        String updateAvailable = versionCheckSystem.isNewVersionAvailable() ? yes : no;
        String connectedToBungee = connectionSystem.isServerAvailable() ? yes : no;
        String[] messages = {
                locale.getString(CommandLang.HEADER_INFO),
                "",
                locale.getString(CommandLang.INFO_VERSION, plugin.getVersion()),
                locale.getString(CommandLang.INFO_UPDATE, updateAvailable),
                locale.getString(CommandLang.INFO_DATABASE, database.getName()),
                locale.getString(CommandLang.INFO_BUNGEE_CONNECTION, connectedToBungee),
                "",
                ">"
        };
        sender.sendMessage(messages);
    }

}
