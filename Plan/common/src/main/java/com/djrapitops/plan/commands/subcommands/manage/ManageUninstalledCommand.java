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
package com.djrapitops.plan.commands.subcommands.manage;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.commands.SetServerAsUninstalledTransaction;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * This SubCommand is used to set a server as uninstalled on Plan.
 *
 * @author Rsl1122
 */
@Singleton
public class ManageUninstalledCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;
    private final ServerInfo serverInfo;

    @Inject
    public ManageUninstalledCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            ErrorLogger errorLogger
    ) {
        super("uninstalled", Permissions.MANAGE.getPermission(), CommandType.ALL_WITH_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.errorLogger = errorLogger;

        setShortHelp(locale.getString(CmdHelpLang.MANAGE_UNINSTALLED));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_UNINSTALLED));
        setArguments("[server/id]");
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        processing.submitNonCritical(() -> {
            try {
                Optional<Server> serverOptional = getServer(args);
                if (!serverOptional.isPresent()) {
                    sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, locale.getString(ManageLang.NO_SERVER)));
                    return;
                }
                Server server = serverOptional.get();
                UUID serverUUID = server.getUuid();
                if (serverInfo.getServerUUID().equals(serverUUID)) {
                    sender.sendMessage(locale.getString(ManageLang.UNINSTALLING_SAME_SERVER));
                    return;
                }

                dbSystem.getDatabase().executeTransaction(new SetServerAsUninstalledTransaction(serverUUID));
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (DBOpException e) {
                sender.sendMessage("§cError occurred: " + e.toString());
                errorLogger.log(L.ERROR, this.getClass(), e);
            }
        });
    }

    private Optional<Server> getServer(String[] args) {
        if (args.length >= 1) {
            String serverIdentifier = getGivenIdentifier(args);
            return dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverIdentifier))
                    .filter(Server::isNotProxy);
        }
        return Optional.empty();
    }

    private String getGivenIdentifier(String[] args) {
        StringBuilder idBuilder = new StringBuilder(args[0]);
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                idBuilder.append(" ").append(args[i]);
            }
        }
        return idBuilder.toString();
    }
}
