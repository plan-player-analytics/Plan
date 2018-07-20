package com.djrapitops.plan.common.command.commands;

import com.djrapitops.plan.common.PlanPlugin;
import com.djrapitops.plan.common.system.database.databases.Database;
import com.djrapitops.plan.common.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.common.system.settings.Permissions;
import com.djrapitops.plan.common.system.settings.locale.Locale;
import com.djrapitops.plan.common.system.settings.locale.Msg;
import com.djrapitops.plan.common.system.update.VersionCheckSystem;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.settings.ColorScheme;

/**
 * This SubCommand is used to view the version and the database type in use.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class InfoCommand extends CommandNode {

    private final PlanPlugin plugin;

    public InfoCommand(PlanPlugin plugin) {
        super("info", Permissions.INFO.getPermission(), CommandType.CONSOLE);
        setShortHelp(Locale.get(Msg.CMD_USG_INFO).toString());
        this.plugin = plugin;
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        ColorScheme cs = plugin.getColorScheme();
        String mColor = cs.getMainColor();
        String sColor = cs.getSecondaryColor();
        String tColor = cs.getTertiaryColor();
        String ball = Locale.get(Msg.CMD_CONSTANT_LIST_BALL).toString();

        String upToDate = VersionCheckSystem.isNewVersionAvailable() ? "Update Available" : "Up to date";
        String[] messages = {
                Locale.get(Msg.CMD_HEADER_INFO).toString(),
                ball + mColor + " Version: " + sColor + plugin.getVersion(),
                ball + mColor + " Up to date: " + sColor + upToDate,
                ball + mColor + " Active Database: " + tColor + Database.getActive().getConfigName(),
                ball + mColor + " Connected to Bungee: " + tColor + (ConnectionSystem.getInstance().isServerAvailable() ? "Yes" : "No"),
                Locale.get(Msg.CMD_CONSTANT_FOOTER).toString()
        };
        sender.sendMessage(messages);
    }

}
