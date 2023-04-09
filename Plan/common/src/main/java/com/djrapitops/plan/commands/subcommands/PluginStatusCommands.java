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

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.gathering.listeners.Status;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.version.VersionChecker;
import net.playeranalytics.plugin.PluginInformation;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PluginStatusCommands {

    private final PlanPlugin plugin;
    private final PluginInformation pluginInformation;
    private final Locale locale;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final Status status;
    private final VersionChecker versionChecker;
    private final ErrorLogger errorLogger;

    @Inject
    public PluginStatusCommands(
            PlanPlugin plugin,
            PluginInformation pluginInformation,
            Locale locale,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            Status status,
            VersionChecker versionChecker,
            ErrorLogger errorLogger
    ) {
        this.plugin = plugin;
        this.pluginInformation = pluginInformation;
        this.locale = locale;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.status = status;
        this.versionChecker = versionChecker;
        this.errorLogger = errorLogger;
    }

    public void onReload(CMDSender sender) {
        new Thread(() -> {
            try {
                plugin.onDisable();
                plugin.onEnable();
                sender.send(locale.getString(CommandLang.RELOAD_COMPLETE));
            } catch (Exception e) {
                errorLogger.critical(e, ErrorContext.builder().related(sender, "reload", Thread.currentThread().getName()).build());
                sender.send(locale.getString(CommandLang.RELOAD_FAILED));
            } finally {
                Thread.currentThread().interrupt();
            }
        }, "Plan Reload Thread").start();
    }

    public void onDisable(CMDSender sender, @Untrusted Arguments arguments) {
        if (arguments.isEmpty()) {
            plugin.onDisable();
            sender.send(locale.getString(CommandLang.DISABLE_DISABLED));
            return;
        }

        boolean kickCountDisable = arguments.get(0).map("kickcount"::equalsIgnoreCase).orElse(false);
        if (kickCountDisable) {
            status.setCountKicks(false);
            sender.send(locale.getString(CommandLang.FEATURE_DISABLED, "Kick Counting"));
        } else {
            sender.send(locale.getString(CommandLang.FAIL_NO_SUCH_FEATURE, "'kickcount'"));
        }
    }

    public void onInfo(CMDSender sender) {
        String yes = locale.getString(GenericLang.YES);
        String no = locale.getString(GenericLang.NO);

        Database database = dbSystem.getDatabase();

        String updateAvailable = versionChecker.isNewVersionAvailable() ? yes : no;
        String proxyAvailable = database.query(ServerQueries.fetchProxyServers()).isEmpty() ? no : yes;


        String[] messages = {
                locale.getString(CommandLang.HEADER_INFO),
                "",
                locale.getString(CommandLang.INFO_VERSION, pluginInformation.getVersion()),
                locale.getString(CommandLang.INFO_UPDATE, updateAvailable),
                locale.getString(CommandLang.INFO_DATABASE, database.getType().getName() + " (" + database.getState().name() + ")"),
                locale.getString(CommandLang.INFO_PROXY_CONNECTION, proxyAvailable),
                locale.getString(CommandLang.INFO_SERVER_UUID, serverInfo.getServerUUID()),
                "",
                ">"
        };
        sender.send(messages);
    }
}
