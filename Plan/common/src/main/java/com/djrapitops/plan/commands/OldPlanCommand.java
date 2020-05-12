/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.commands;

import com.djrapitops.plan.commands.subcommands.*;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * TreeCommand for the /plan command, and all SubCommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 */
@Singleton
public class OldPlanCommand extends TreeCmdNode {

    private final PlanConfig config;
    private final InspectCommand inspectCommand;
    private final QInspectCommand qInspectCommand;
    private final SearchCommand searchCommand;
    private final ListPlayersCommand listPlayersCommand;
    private final AnalyzeCommand analyzeCommand;
    private final NetworkCommand networkCommand;
    private final ListServersCommand listServersCommand;
    private final Lazy<WebUserCommand> webUserCommand;
    private final RegisterCommand registerCommand;
    private final UnregisterCommand unregisterCommand;
    private final InfoCommand infoCommand;
    private final ReloadCommand reloadCommand;
    private final Lazy<ManageCommand> manageCommand;
    private final DevCommand devCommand;

    private boolean commandsRegistered;

    @Inject
    public OldPlanCommand(
            ColorScheme colorScheme,
            Locale locale,
            PlanConfig config,
            // Group 1
            InspectCommand inspectCommand,
            QInspectCommand qInspectCommand,
            SearchCommand searchCommand,
            ListPlayersCommand listPlayersCommand,
            AnalyzeCommand analyzeCommand,
            NetworkCommand networkCommand,
            ListServersCommand listServersCommand,
            // Group 2
            Lazy<WebUserCommand> webUserCommand,
            RegisterCommand registerCommand,
            UnregisterCommand unregisterCommand,
            // Group 3
            InfoCommand infoCommand,
            ReloadCommand reloadCommand,
            Lazy<ManageCommand> manageCommand,
            DevCommand devCommand
    ) {
        super("plan", "", CommandType.CONSOLE, null);
        this.unregisterCommand = unregisterCommand;

        commandsRegistered = false;

        this.config = config;
        this.inspectCommand = inspectCommand;
        this.qInspectCommand = qInspectCommand;
        this.searchCommand = searchCommand;
        this.listPlayersCommand = listPlayersCommand;
        this.analyzeCommand = analyzeCommand;
        this.networkCommand = networkCommand;
        this.listServersCommand = listServersCommand;
        this.webUserCommand = webUserCommand;
        this.registerCommand = registerCommand;
        this.infoCommand = infoCommand;
        this.reloadCommand = reloadCommand;
        this.manageCommand = manageCommand;
        this.devCommand = devCommand;

        getHelpCommand().setPermission(Permissions.HELP.getPermission());
        setDefaultCommand("inspect");
        setColorScheme(colorScheme);
        setInDepthHelp(locale.getArray(DeepHelpLang.PLAN));
    }

    public void registerCommands() {
        if (commandsRegistered) {
            return;
        }

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
                webUserCommand.get(),
                registerCommand,
                unregisterCommand
        };
        CommandNode[] manageGroup = {
                infoCommand,
                reloadCommand,
                manageCommand.get(),
                config.isTrue(PluginSettings.DEV_MODE) ? devCommand : null
        };
        setNodeGroups(analyticsGroup, webGroup, manageGroup);
        commandsRegistered = true;
    }
}
