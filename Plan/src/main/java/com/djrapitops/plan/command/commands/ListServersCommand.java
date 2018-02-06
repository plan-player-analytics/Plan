package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;

import java.util.Map;

/**
 * This SubCommand is used to list all servers found in the database.
 *
 * @author Rsl1122
 */
public class ListServersCommand extends SubCommand {

    private final PlanPlugin plugin;

    public ListServersCommand(PlanPlugin plugin) {
        super("servers, serverlist, listservers, sl",
                CommandType.CONSOLE,
                Permissions.MANAGE.getPermission(),
                "List servers in the network");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        ColorScheme colorScheme = plugin.getColorScheme();
        String mCol = colorScheme.getMainColor();
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();
        try {
            sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString() + mCol + " Servers");
            Map<Integer, String> serverNames = Database.getActive().fetch().getServerNamesByID();
            for (Map.Entry<Integer, String> entry : serverNames.entrySet()) {
                sender.sendMessage("  " + tCol + entry.getKey() + sCol + " : " + entry.getValue());
            }
            sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
        } catch (DBException e) {
            sender.sendMessage("§cDatabase Exception occurred.");
            Log.toLog(this.getClass().getName(), e);
        }
        return true;
    }

}
