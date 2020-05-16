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

import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * TreeCommand for the /plan command, and all subcommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 */
@Singleton
public class PlanProxyCommand extends TreeCmdNode {

    private boolean commandsRegistered;

    @Inject
    public PlanProxyCommand(
            @Named("mainCommandName") String mainCommandName,
            ColorScheme colorScheme,
            Locale locale
            // Group 1
            // Group 2
            // Group 3
    ) {
        super(mainCommandName, Permissions.MANAGE.getPermission(), CommandType.CONSOLE, null);

        commandsRegistered = false;

        getHelpCommand().setPermission(Permissions.MANAGE.getPermission());
        setColorScheme(colorScheme);
        setInDepthHelp(locale.getArray(DeepHelpLang.PLAN));
    }

    public void registerCommands() {
        if (commandsRegistered) {
            return;
        }

        CommandNode[] analyticsGroup = {
                // networkCommand,
                // listPlayersCommand,
                // list servers
        };
        CommandNode[] webGroup = {
                // registerCommand,
                // unregisterCommand,
                // webUserCommand.get()
        };
        CommandNode[] manageGroup = {
                // rawDataCommand,
                // uninstalledCommand,
                //reloadCommand,
                //disableCommand
        };
        setNodeGroups(analyticsGroup, webGroup, manageGroup);
        commandsRegistered = true;
    }
}
