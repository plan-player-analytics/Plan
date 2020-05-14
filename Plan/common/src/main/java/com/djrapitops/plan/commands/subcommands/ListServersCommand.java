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
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;

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
    private final ErrorLogger errorLogger;

    @Inject
    public ListServersCommand(
            Locale locale,
            ColorScheme colorScheme,
            DBSystem dbSystem,
            ErrorLogger errorLogger
    ) {
        super("servers|serverlist|listservers|sl|ls", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.colorScheme = colorScheme;
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;

        setShortHelp(locale.getString(CmdHelpLang.SERVERS));
        setInDepthHelp(locale.getArray(DeepHelpLang.SERVERS));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();
        Formatter<Server> serverFormatter = serverLister(sCol, tCol);
        try {
            sender.sendMessage(locale.getString(CommandLang.HEADER_SERVERS));
            sendServers(sender, serverFormatter);
            sender.sendMessage(">");
        } catch (DBOpException e) {
            sender.sendMessage("§cDatabase Exception occurred.");
            errorLogger.log(L.WARN, this.getClass(), e);
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
