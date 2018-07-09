/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

/**
 * Command for Toggling whether or not BungeeCord accepts set up requests.
 * <p>
 * This was added as a security measure against unwanted MySQL snooping.
 *
 * @author Rsl1122
 */
public class BungeeSetupToggleCommand extends CommandNode {

    public BungeeSetupToggleCommand() {
        super("setup", Permissions.MANAGE.getPermission(), CommandType.ALL);
        setShortHelp("Toggle Setup mode for Bungee");
    }

    @Override
    public void onCommand(ISender sender, String s, String[] strings) {
        boolean setupAllowed = ConnectionSystem.isSetupAllowed();
        ConnectionSystem connectionSystem = ConnectionSystem.getInstance();

        if (setupAllowed) {
            connectionSystem.setSetupAllowed(false);
        } else {
            connectionSystem.setSetupAllowed(true);
        }
        String msg = !setupAllowed ? "§aSet-up is now Allowed" : "§cSet-up is now Forbidden";
        sender.sendMessage(msg);
    }
}
