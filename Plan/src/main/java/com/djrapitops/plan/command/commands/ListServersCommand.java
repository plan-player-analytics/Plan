package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.settings.ColorScheme;

import java.util.List;

/**
 * This SubCommand is used to list all servers found in the database.
 *
 * @author Rsl1122
 */
public class ListServersCommand extends CommandNode {

    private final PlanPlugin plugin;
    private final Locale locale;

    public ListServersCommand(PlanPlugin plugin) {
        super("servers|serverlist|listservers|sl|ls", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);
        this.plugin = plugin;

        this.locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.SERVERS));
        setInDepthHelp(locale.getArray(DeepHelpLang.SERVERS));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        ColorScheme colorScheme = plugin.getColorScheme();
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();
        try {
            sender.sendMessage(locale.getString(CommandLang.HEADER_SERVERS));
            List<Server> servers = Database.getActive().fetch().getServers();
            for (Server server : servers) {
                sender.sendMessage("  " + tCol + server.getId() + sCol + " : " + server.getName() + " : " + server.getWebAddress());
            }
            sender.sendMessage(">");
        } catch (DBOpException e) {
            sender.sendMessage("§cDatabase Exception occurred.");
            Log.toLog(this.getClass(), e);
        }
    }

}
