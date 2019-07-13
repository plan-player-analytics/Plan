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
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.db.access.queries.objects.WebUserQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.export.HtmlExport;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * This SubCommand is used to run the analysis and access the /server link.
 *
 * @author Rsl1122
 */
@Singleton
public class AnalyzeCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final HtmlExport export;
    private final ServerInfo serverInfo;
    private final WebServer webServer;
    private final DBSystem dbSystem;
    private final ConnectionSystem connectionSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public AnalyzeCommand(
            Locale locale,
            Processing processing,
            HtmlExport export,
            ServerInfo serverInfo,
            ConnectionSystem connectionSystem,
            WebServer webServer,
            DBSystem dbSystem,
            ErrorHandler errorHandler
    ) {
        super("analyze|analyse|analysis|a", Permissions.ANALYZE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.processing = processing;
        this.export = export;
        this.serverInfo = serverInfo;
        this.connectionSystem = connectionSystem;
        this.webServer = webServer;
        this.dbSystem = dbSystem;
        this.errorHandler = errorHandler;

        setShortHelp(locale.getString(CmdHelpLang.ANALYZE));
        setInDepthHelp(locale.getArray(DeepHelpLang.ANALYZE));
        setArguments("[server/id]");
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        processing.submitNonCritical(() -> {
            try {
                Server server = getServer(args).orElseGet(serverInfo::getServer);
                sendWebUserNotificationIfNecessary(sender);
                if (connectionSystem.isServerAvailable()) {
                    export.exportServer(server.getUuid());
                }
                sendLink(server, sender);
            } catch (DBOpException e) {
                sender.sendMessage("§cError occurred: " + e.toString());
                errorHandler.log(L.ERROR, this.getClass(), e);
            }
        });
    }

    private void sendLink(Server server, Sender sender) {
        String target = "/server/" + server.getName();
        String url = connectionSystem.getMainAddress() + target;
        String linkPrefix = locale.getString(CommandLang.LINK_PREFIX);
        sender.sendMessage(locale.getString(CommandLang.HEADER_ANALYSIS));
        // Link
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(linkPrefix + url);
        } else {
            sender.sendMessage(linkPrefix);
            sender.sendLink("   ", locale.getString(CommandLang.LINK_CLICK_ME), url);
        }
        sender.sendMessage(">");
    }

    private void sendWebUserNotificationIfNecessary(Sender sender) {
        if (webServer.isAuthRequired() &&
                CommandUtils.isPlayer(sender) &&
                !dbSystem.getDatabase().query(WebUserQueries.fetchWebUser(sender.getName())).isPresent()) {
            sender.sendMessage("§e" + locale.getString(CommandLang.NO_WEB_USER_NOTIFY));
        }
    }

    private Optional<Server> getServer(String[] args) {
        if (args.length >= 1 && connectionSystem.isServerAvailable()) {
            String serverIdentifier = getGivenIdentifier(args);
            return dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverIdentifier))
                    .filter(server -> !server.isProxy());
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
