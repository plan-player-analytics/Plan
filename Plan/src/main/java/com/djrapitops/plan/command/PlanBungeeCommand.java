package com.djrapitops.plan.command;

import com.djrapitops.plan.command.commands.*;
import com.djrapitops.plan.command.commands.manage.ManageConDebugCommand;
import com.djrapitops.plan.command.commands.manage.ManageRawDataCommand;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;

import javax.inject.Inject;

/**
 * TreeCommand for the /plan command, and all subcommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class PlanBungeeCommand extends TreeCmdNode {

    @Inject
    public PlanBungeeCommand(ColorScheme colorScheme, Locale locale,
                             // Group 1
                             NetworkCommand networkCommand,
                             ListServersCommand listServersCommand,
                             ListPlayersCommand listPlayersCommand,
                             // Group 2
                             RegisterCommand registerCommand,
                             WebUserCommand webUserCommand,
                             // Group 3
                             ManageConDebugCommand conDebugCommand,
                             ManageRawDataCommand rawDataCommand,
                             BungeeSetupToggleCommand setupToggleCommand,
                             ReloadCommand reloadCommand,
                             DisableCommand disableCommand
    ) {
        super("planbungee", Permissions.MANAGE.getPermission(), CommandType.CONSOLE, null);
        super.setColorScheme(colorScheme);

        setInDepthHelp(locale.getArray(DeepHelpLang.PLAN));

        CommandNode[] analyticsGroup = {
                networkCommand,
                listServersCommand,
                listPlayersCommand
        };
        CommandNode[] webGroup = {
                registerCommand,
                webUserCommand
        };
        CommandNode[] manageGroup = {
                conDebugCommand,
                rawDataCommand,
                setupToggleCommand,
                reloadCommand,
                disableCommand
        };
        setNodeGroups(analyticsGroup, webGroup, manageGroup);
    }
}
