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
package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;

import javax.inject.Inject;

/**
 * Command for Toggling whether or not BungeeCord accepts set up requests.
 * <p>
 * This was added as a security measure against unwanted MySQL snooping.
 *
 * @author Rsl1122
 */
public class BungeeSetupToggleCommand extends CommandNode {

    private final Locale locale;
    private final ConnectionSystem connectionSystem;

    @Inject
    public BungeeSetupToggleCommand(Locale locale, ConnectionSystem connectionSystem) {
        super("setup", Permissions.MANAGE.getPermission(), CommandType.ALL);

        this.locale = locale;
        this.connectionSystem = connectionSystem;

        setShortHelp(locale.getString(CmdHelpLang.SETUP));
        setInDepthHelp(locale.getArray(DeepHelpLang.SETUP));
    }

    @Override
    public void onCommand(Sender sender, String s, String[] strings) {
        if (connectionSystem.isSetupAllowed()) {
            connectionSystem.setSetupAllowed(false);
        } else {
            connectionSystem.setSetupAllowed(true);
        }

        String msg = locale.getString(connectionSystem.isSetupAllowed() ? CommandLang.SETUP_ALLOWED : CommandLang.CONNECT_FORBIDDEN);
        sender.sendMessage(msg);
    }
}
