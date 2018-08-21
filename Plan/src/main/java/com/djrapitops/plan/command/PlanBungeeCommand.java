package com.djrapitops.plan.command;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.command.commands.*;
import com.djrapitops.plan.command.commands.manage.ManageConDebugCommand;
import com.djrapitops.plan.command.commands.manage.ManageRawDataCommand;
import com.djrapitops.plan.command.commands.manage.ManageUninstalledCommand;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;
import com.djrapitops.plugin.command.defaultcmds.StatusCommand;

/**
 * TreeCommand for the /plan command, and all subcommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class PlanBungeeCommand extends TreeCmdNode {

    public PlanBungeeCommand(PlanPlugin plugin) {
        super("planbungee", Permissions.MANAGE.getPermission(), CommandType.CONSOLE, null);
        super.setColorScheme(plugin.getColorScheme());

        Locale locale = plugin.getSystem().getLocaleSystem().getLocale();

        setInDepthHelp(locale.getArray(DeepHelpLang.PLAN));

        RegisterCommand registerCommand = new RegisterCommand(plugin);
        CommandNode[] analyticsGroup = {
                new NetworkCommand(plugin),
                new ListServersCommand(plugin),
                new ListPlayersCommand(plugin),
        };
        CommandNode[] webGroup = {
                registerCommand,
                new WebUserCommand(plugin, registerCommand, this),
        };
        CommandNode[] manageGroup = {
                new ManageConDebugCommand(plugin),
                new ManageRawDataCommand(plugin),
                new BungeeSetupToggleCommand(plugin),
                new ManageUninstalledCommand(plugin),
                new ReloadCommand(plugin),
                new DisableCommand(plugin),
                new StatusCommand<>(plugin, Permissions.MANAGE.getPermission(), plugin.getColorScheme()),
//                        (Settings.ALLOW_UPDATE.isTrue() ? new UpdateCommand() : null)
        };
        setNodeGroups(analyticsGroup, webGroup, manageGroup);
    }
}
