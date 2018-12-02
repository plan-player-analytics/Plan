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
package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.command.commands.webuser.WebCheckCommand;
import com.djrapitops.plan.command.commands.webuser.WebDeleteCommand;
import com.djrapitops.plan.command.commands.webuser.WebLevelCommand;
import com.djrapitops.plan.command.commands.webuser.WebListUsersCommand;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Web subcommand used to manage Web users.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebUserCommand extends TreeCmdNode {

    @Inject
    public WebUserCommand(ColorScheme colorScheme, Locale locale, @Named("mainCommand") Lazy<CommandNode> parent,
                          RegisterCommand registerCommand,
                          WebLevelCommand levelCommand,
                          WebListUsersCommand listUsersCommand,
                          WebCheckCommand checkCommand,
                          WebDeleteCommand deleteCommand
    ) {
        super("webuser|web", Permissions.MANAGE_WEB.getPerm(), CommandType.CONSOLE, parent.get());
        super.setColorScheme(colorScheme);

        setShortHelp(locale.getString(CmdHelpLang.WEB));
        setInDepthHelp(locale.getArray(DeepHelpLang.WEB));
        CommandNode[] webGroup = {
                registerCommand,
                levelCommand,
                listUsersCommand,
                checkCommand,
                deleteCommand
        };
        setNodeGroups(webGroup);
    }
}
