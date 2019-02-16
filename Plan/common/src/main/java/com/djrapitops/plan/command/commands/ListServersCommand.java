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

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This SubCommand is used to list all servers found in the database.
 *
 * @author Rsl1122
 */
public class ListServersCommand extends CommandNode {

    private final Locale locale;
    private final ColorScheme colorScheme;
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public ListServersCommand(
            Locale locale,
            ColorScheme colorScheme,
            DBSystem dbSystem,
            ErrorHandler errorHandler
    ) {
        super("servers|serverlist|listservers|sl|ls", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.colorScheme = colorScheme;
        this.dbSystem = dbSystem;
        this.errorHandler = errorHandler;

        setShortHelp(locale.getString(CmdHelpLang.SERVERS));
        setInDepthHelp(locale.getArray(DeepHelpLang.SERVERS));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();
        Formatter<Server> serverFormatter = serverLister(sCol, tCol);
        try {
            sender.sendMessage(locale.getString(CommandLang.HEADER_SERVERS));
            sendServers(sender, serverFormatter);
            sender.sendMessage(">");
        } catch (DBOpException e) {
            sender.sendMessage("§cDatabase Exception occurred.");
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    private void sendServers(Sender sender, Formatter<Server> serverFormatter) {
        List<Server> servers = new ArrayList<>(dbSystem.getDatabase().query(ServerQueries.fetchPlanServerInformation()).values());
        Collections.sort(servers);
        for (Server server : servers) {
            sender.sendMessage(serverFormatter.apply(server));
        }
    }

    private Formatter<Server> serverLister(String tertiaryColor, String secondaryColor) {
        return server -> "  " + tertiaryColor + server.getId() + secondaryColor + " : " + server.getName() + " : " + server.getWebAddress();
    }

}
