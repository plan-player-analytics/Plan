package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.Msg;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.update.VersionCheckSystem;
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
    private final Locale locale;

    public InfoCommand(PlanPlugin plugin) {
        super("info", Permissions.INFO.getPermission(), CommandType.CONSOLE);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.get(CmdHelpLang.INFO).toString());
        this.plugin = plugin;
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        ColorScheme cs = plugin.getColorScheme();
        String mColor = cs.getMainColor();
        String sColor = cs.getSecondaryColor();
        String tColor = cs.getTertiaryColor();

        String upToDate = VersionCheckSystem.isNewVersionAvailable() ? "Update Available" : "Up to date";
        String[] messages = {
                locale.get(Msg.CMD_HEADER_INFO).toString(),
                mColor + "  Version: " + sColor + plugin.getVersion(),
                mColor + "  Up to date: " + sColor + upToDate,
                mColor + "  Active Database: " + tColor + Database.getActive().getConfigName(),
                mColor + "  Connected to Bungee: " + tColor + (ConnectionSystem.getInstance().isServerAvailable() ? "Yes" : "No"),
                locale.get(Msg.CMD_CONSTANT_FOOTER).toString()
        };
        sender.sendMessage(messages);
    }

}
