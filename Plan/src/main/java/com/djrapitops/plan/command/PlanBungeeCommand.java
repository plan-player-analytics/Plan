package com.djrapitops.plan.command;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.command.commands.*;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;
import com.djrapitops.plugin.command.defaultcmds.StatusCommand;

/**
 * TreeCommand for the /plan command, and all subcommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class PlanBungeeCommand extends TreeCommand<PlanBungee> {

    /**
     * CommandExecutor class Constructor.
     * <p>
     * Initializes Subcommands
     *
     * @param plugin Current instance of Plan
     */
    public PlanBungeeCommand(PlanBungee plugin) {
        super(plugin, "planbungee", CommandType.CONSOLE, Permissions.MANAGE.getPermission(), "", "planbungee");
        super.setDefaultCommand("help");
        super.setColorScheme(plugin.getColorScheme());
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_PLAN).toArray();
    }

    @Override
    public void addCommands() {
        add(
                new ReloadCommand(plugin),
                new StatusCommand<>(plugin, Permissions.MANAGE.getPermission(), plugin.getColorScheme()),
                new ListCommand(),
                new BungeeSetupToggleCommand(plugin)
        );
        RegisterCommand registerCommand = new RegisterCommand(plugin);
        add(
                registerCommand,
                new WebUserCommand(plugin, registerCommand),
                new NetworkCommand(plugin),
                new ListServersCommand(plugin)
        );
    }
}
