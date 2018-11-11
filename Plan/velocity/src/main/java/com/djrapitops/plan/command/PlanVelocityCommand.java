/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.command;

import com.djrapitops.plan.command.commands.*;
import com.djrapitops.plan.command.commands.manage.ManageConDebugCommand;
import com.djrapitops.plan.command.commands.manage.ManageRawDataCommand;
import com.djrapitops.plan.command.commands.manage.ManageUninstalledCommand;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * TreeCommand for the /plan command, and all subcommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
@Singleton
public class PlanVelocityCommand extends TreeCmdNode {

    private final NetworkCommand networkCommand;
    private final ListServersCommand listServersCommand;
    private final ListPlayersCommand listPlayersCommand;
    private final RegisterCommand registerCommand;
    private final Lazy<WebUserCommand> webUserCommand;
    private final ManageConDebugCommand conDebugCommand;
    private final ManageRawDataCommand rawDataCommand;
    private final BungeeSetupToggleCommand setupToggleCommand;
    private final ReloadCommand reloadCommand;
    private final DisableCommand disableCommand;
    private final ManageUninstalledCommand uninstalledCommand;

    private boolean commandsRegistered;

    @Inject
    public PlanVelocityCommand(
            ColorScheme colorScheme,
            Locale locale,
            // Group 1
            NetworkCommand networkCommand,
            ListServersCommand listServersCommand,
            ListPlayersCommand listPlayersCommand,
            // Group 2
            RegisterCommand registerCommand,
            Lazy<WebUserCommand> webUserCommand,
            // Group 3
            ManageConDebugCommand conDebugCommand,
            ManageRawDataCommand rawDataCommand,
            BungeeSetupToggleCommand setupToggleCommand,
            ManageUninstalledCommand uninstalledCommand,
            ReloadCommand reloadCommand,
            DisableCommand disableCommand
    ) {
        super("planvelocity", Permissions.MANAGE.getPermission(), CommandType.CONSOLE, null);
        this.uninstalledCommand = uninstalledCommand;

        commandsRegistered = false;

        this.networkCommand = networkCommand;
        this.listServersCommand = listServersCommand;
        this.listPlayersCommand = listPlayersCommand;
        this.registerCommand = registerCommand;
        this.webUserCommand = webUserCommand;
        this.conDebugCommand = conDebugCommand;
        this.rawDataCommand = rawDataCommand;
        this.setupToggleCommand = setupToggleCommand;
        this.reloadCommand = reloadCommand;
        this.disableCommand = disableCommand;

        getHelpCommand().setPermission(Permissions.MANAGE.getPermission());
        setColorScheme(colorScheme);
        setInDepthHelp(locale.getArray(DeepHelpLang.PLAN));
    }

    public void registerCommands() {
        if (commandsRegistered) {
            return;
        }

        CommandNode[] analyticsGroup = {
                networkCommand,
                listServersCommand,
                listPlayersCommand
        };
        CommandNode[] webGroup = {
                registerCommand,
                webUserCommand.get()
        };
        CommandNode[] manageGroup = {
                conDebugCommand,
                rawDataCommand,
                setupToggleCommand,
                uninstalledCommand,
                reloadCommand,
                disableCommand
        };
        setNodeGroups(analyticsGroup, webGroup, manageGroup);
        commandsRegistered = true;
    }
}
