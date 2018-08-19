package com.djrapitops.plan.command;

import com.djrapitops.plan.command.commands.*;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;

import javax.inject.Inject;

/**
 * TreeCommand for the /plan command, and all SubCommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class PlanCommand extends TreeCmdNode {

    @Inject
    public PlanCommand(ColorScheme colorScheme, Locale locale, PlanConfig config,
                       // Group 1
                       InspectCommand inspectCommand,
                       QInspectCommand qInspectCommand,
                       SearchCommand searchCommand,
                       ListPlayersCommand listPlayersCommand,
                       AnalyzeCommand analyzeCommand,
                       NetworkCommand networkCommand,
                       ListServersCommand listServersCommand,
                       // Group 2
                       WebUserCommand webUserCommand,
                       RegisterCommand registerCommand,
                       // Group 3
                       InfoCommand infoCommand,
                       ReloadCommand reloadCommand,
                       ManageCommand manageCommand,
                       DevCommand devCommand
    ) {
        super("plan", "", CommandType.CONSOLE, null);
        super.setDefaultCommand("inspect");
        super.setColorScheme(colorScheme);

        setInDepthHelp(locale.getArray(DeepHelpLang.PLAN));

        CommandNode[] analyticsGroup = {
                inspectCommand,
                qInspectCommand,
                searchCommand,
                listPlayersCommand,
                analyzeCommand,
                networkCommand,
                listServersCommand
        };
        CommandNode[] webGroup = {
                webUserCommand,
                registerCommand
        };
        CommandNode[] manageGroup = {
                infoCommand,
                reloadCommand,
                manageCommand,
                config.isTrue(Settings.DEV_MODE) ? devCommand : null
        };
        setNodeGroups(analyticsGroup, webGroup, manageGroup);
    }
}
