package com.djrapitops.plan.bungee.command;

import com.djrapitops.plan.bungee.PlanBungee;
import com.djrapitops.plan.command.commands.*;
import com.djrapitops.plan.command.commands.manage.ManageConDebugCommand;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
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

    /**
     * CommandExecutor class Constructor.
     * <p>
     * Initializes Subcommands
     *
     * @param plugin Current instance of Plan
     */
    public PlanBungeeCommand(PlanBungee plugin) {
        super("planbungee", Permissions.MANAGE.getPermission(), CommandType.CONSOLE, null);
        super.setColorScheme(plugin.getColorScheme());
        setInDepthHelp(Locale.get(Msg.CMD_HELP_PLAN).toArray());

        RegisterCommand registerCommand = new RegisterCommand();
        setNodeGroups(
                new CommandNode[]{
                        new NetworkCommand(),
                        new ListServersCommand(plugin),
                        new ListCommand(),
                },
                new CommandNode[]{
                        registerCommand,
                        new WebUserCommand(plugin, registerCommand, this),
                },
                new CommandNode[]{
                        new ManageConDebugCommand(),
                        new BungeeSetupToggleCommand(),
                        new ReloadCommand(plugin),
                        new DisableCommand(plugin),
                        new StatusCommand<>(plugin, Permissions.MANAGE.getPermission(), plugin.getColorScheme()),
//                        (Settings.ALLOW_UPDATE.isTrue() ? new UpdateCommand() : null)
                }
        );
    }
}
