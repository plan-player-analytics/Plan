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
package com.djrapitops.plan.commands.subcommands.webuser;

import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;

import javax.inject.Inject;

/**
 * Subcommand for info about permission levels.
 *
 * @author Rsl1122
 */
public class WebLevelCommand extends CommandNode {

    private final Locale locale;

    @Inject
    public WebLevelCommand(Locale locale) {
        super("level", Permissions.MANAGE_WEB.getPerm(), CommandType.CONSOLE);

        this.locale = locale;

        setShortHelp(locale.getString(CmdHelpLang.WEB_LEVEL));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        sender.sendMessage(locale.getArray(CommandLang.WEB_PERMISSION_LEVELS));
    }

}
