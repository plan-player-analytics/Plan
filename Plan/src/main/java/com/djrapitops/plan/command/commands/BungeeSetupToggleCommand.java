/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;

/**
 * Command for Toggling whether or not BungeeCord accepts set up requests.
 *
 * This was added as a security measure against unwanted MySQL snooping.
 *
 * @author Rsl1122
 */
public class BungeeSetupToggleCommand extends SubCommand {

    private final PlanBungee plugin;

    public BungeeSetupToggleCommand(PlanBungee plugin) {
        super("setup", CommandType.ALL, Permissions.MANAGE.getPermission(), "Toggle Setup mode for Bungee");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String s, String[] strings) {
        boolean setupAllowed = plugin.isSetupAllowed();
        if (setupAllowed) {
            plugin.setSetupAllowed(false);
        } else {
            plugin.setSetupAllowed(true);
        }
        String msg = !setupAllowed ? "§aSet-up is now Allowed" : "§cSet-up is now Forbidden";
        sender.sendMessage(msg);
        return true;
    }
}