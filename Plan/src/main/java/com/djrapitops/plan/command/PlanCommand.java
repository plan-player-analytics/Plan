package com.djrapitops.plan.command;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.commands.*;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
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
public class PlanCommand extends TreeCommand<Plan> {

    /**
     * CommandExecutor class Constructor.
     * <p>
     * Initializes Subcommands
     *
     * @param plugin Current instance of Plan
     */
    public PlanCommand(Plan plugin) {
        super(plugin, "plan", CommandType.CONSOLE, "", "", "plan");
        super.setDefaultCommand("inspect");
        super.setColorScheme(plugin.getColorScheme());
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_PLAN).toArray();
    }

    @Override
    public void addCommands() {
        add(
                new InspectCommand(plugin),
                new QInspectCommand(plugin),
                new AnalyzeCommand(),
                new SearchCommand(),
                new InfoCommand(plugin),
                new ReloadCommand(plugin),
                new ManageCommand(plugin),
                new StatusCommand<>(plugin, Permissions.MANAGE.getPermission(), plugin.getColorScheme()),
                new ListCommand()
        );
        RegisterCommand registerCommand = new RegisterCommand();
        add(
                registerCommand,
                new WebUserCommand(plugin, registerCommand),
                new NetworkCommand(),
                new ListServersCommand(plugin));

        if (Settings.DEV_MODE.isTrue()) {
            add(new DevCommand());
        }
    }
}
