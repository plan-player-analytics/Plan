package com.djrapitops.plan.command;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.command.commands.*;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.Msg;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;
import com.djrapitops.plugin.command.defaultcmds.StatusCommand;

/**
 * TreeCommand for the /plan command, and all SubCommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class PlanCommand extends TreeCmdNode {

    public PlanCommand(PlanPlugin plugin) {
        super("plan", "", CommandType.CONSOLE, null);
        super.setDefaultCommand("inspect");
        super.setColorScheme(plugin.getColorScheme());

        Locale locale = plugin.getSystem().getLocaleSystem().getLocale();

        setInDepthHelp(locale.getArray(Msg.CMD_HELP_PLAN));

        RegisterCommand registerCommand = new RegisterCommand(plugin);
        setNodeGroups(
                new CommandNode[]{
                        new InspectCommand(plugin),
                        new QInspectCommand(plugin),
                        new SearchCommand(plugin),
                        new ListCommand(plugin),
                        new AnalyzeCommand(plugin),
                        new NetworkCommand(plugin),
                        new ListServersCommand(plugin)
                },
                new CommandNode[]{
                        new WebUserCommand(plugin, registerCommand, this),
                        registerCommand
                },
                new CommandNode[]{
                        new InfoCommand(plugin),
                        new ReloadCommand(plugin),
                        new ManageCommand(plugin, this),
                        new StatusCommand<>(plugin, Permissions.MANAGE.getPermission(), plugin.getColorScheme()),
                        (Settings.DEV_MODE.isTrue() ? new DevCommand(plugin) : null),
//                        (Settings.ALLOW_UPDATE.isTrue() ? new UpdateCommand() : null)
                }
        );
    }
}
