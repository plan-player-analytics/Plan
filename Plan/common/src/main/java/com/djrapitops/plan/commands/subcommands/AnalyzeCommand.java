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

import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.delivery.webserver.WebServer;
import com.djrapitops.plan.exceptions.ExportException;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;

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
    private final Exporter exporter;
    private final Addresses addresses;
    private final ServerInfo serverInfo;
    private final WebServer webServer;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    @Inject
    public AnalyzeCommand(
            Locale locale,
            Processing processing,
            Exporter exporter,
            Addresses addresses,
            ServerInfo serverInfo,
            WebServer webServer,
            DBSystem dbSystem,
            ErrorLogger errorLogger
    ) {
        super("analyze|analyse|analysis|a", Permissions.ANALYZE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.processing = processing;
        this.exporter = exporter;
        this.addresses = addresses;
        this.serverInfo = serverInfo;
        this.webServer = webServer;
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;

        setShortHelp(locale.getString(CmdHelpLang.ANALYZE));
        setInDepthHelp(locale.getArray(DeepHelpLang.ANALYZE));
        setArguments("[server/id]");
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Database database = dbSystem.getDatabase();
        Database.State dbState = database.getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        processing.submitNonCritical(() -> {
            try {
                Server server = getServer(args);
                sendWebUserNotificationIfNecessary(sender);
                exporter.exportServerPage(server);
                sendLink(server, sender);
            } catch (DBOpException | ExportException e) {
                sender.sendMessage("§cError occurred: " + e.toString());
                errorLogger.log(L.ERROR, this.getClass(), e);
            }
        });
    }

    private void sendLink(Server server, Sender sender) {
        String target = "/server/" + Html.encodeToURL(server.getName());
        String address = addresses.getMainAddress().orElseGet(() -> {
            sender.sendMessage(locale.getString(CommandLang.NO_ADDRESS_NOTIFY));
            return addresses.getFallbackLocalhostAddress();
        });
        String url = address + target;
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
                !dbSystem.getDatabase().query(WebUserQueries.fetchUserLinkedTo(sender.getName())).isPresent()) {
            sender.sendMessage("§e" + locale.getString(CommandLang.NO_WEB_USER_NOTIFY));
        }
    }

    private Server getServer(String[] args) {
        return getGivenIdentifier(args)
                .flatMap(serverIdentifier -> dbSystem.getDatabase()
                        .query(ServerQueries.fetchServerMatchingIdentifier(serverIdentifier))
                ).filter(server -> !server.isProxy())
                .orElseGet(serverInfo::getServer);
    }

    private Optional<String> getGivenIdentifier(String[] args) {
        if (args.length < 1) {
            return Optional.empty();
        }
        StringBuilder idBuilder = new StringBuilder(args[0]);
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                idBuilder.append(" ").append(args[i]);
            }
        }
        return Optional.of(idBuilder.toString());
    }
}
