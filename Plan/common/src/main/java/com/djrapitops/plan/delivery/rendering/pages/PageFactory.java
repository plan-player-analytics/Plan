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
package com.djrapitops.plan.delivery.rendering.pages;

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.system.version.VersionCheckSystem;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Factory for creating different {@link Page} objects.
 *
 * @author Rsl1122
 */
@Singleton
public class PageFactory {

    private final Lazy<VersionCheckSystem> versionCheckSystem;
    private final Lazy<PlanFiles> fileSystem;
    private final Lazy<PlanConfig> config;
    private final Lazy<Theme> theme;
    private final Lazy<DBSystem> dbSystem;
    private final Lazy<ServerInfo> serverInfo;
    private final Lazy<Formatters> formatters;
    private final Lazy<DebugLogger> debugLogger;
    private final Lazy<Timings> timings;
    private final Lazy<ErrorHandler> errorHandler;

    @Inject
    public PageFactory(
            Lazy<VersionCheckSystem> versionCheckSystem,
            Lazy<PlanFiles> fileSystem,
            Lazy<PlanConfig> config,
            Lazy<Theme> theme,
            Lazy<DBSystem> dbSystem,
            Lazy<ServerInfo> serverInfo,
            Lazy<Formatters> formatters,
            Lazy<DebugLogger> debugLogger,
            Lazy<Timings> timings,
            Lazy<ErrorHandler> errorHandler
    ) {
        this.versionCheckSystem = versionCheckSystem;
        this.fileSystem = fileSystem;
        this.config = config;
        this.theme = theme;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.formatters = formatters;
        this.debugLogger = debugLogger;
        this.timings = timings;
        this.errorHandler = errorHandler;
    }

    public DebugPage debugPage() {
        return new DebugPage(
                dbSystem.get().getDatabase(), serverInfo.get(), formatters.get(), versionCheckSystem.get(),
                debugLogger.get(), timings.get(), errorHandler.get()
        );
    }

    public PlayersPage playersPage() {
        return new PlayersPage(versionCheckSystem.get(), fileSystem.get(), config.get(), serverInfo.get());
    }

    public ServerPage serverPage(UUID serverUUID) throws NotFoundException {
        return dbSystem.get().getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID))
                .map(server -> new ServerPage(
                        server,
                        config.get(),
                        theme.get(),
                        versionCheckSystem.get(),
                        fileSystem.get(),
                        dbSystem.get(),
                        serverInfo.get(),
                        formatters.get()
                )).orElseThrow(() -> new NotFoundException("Server not found in the database"));
    }

    public PlayerPage playerPage(UUID playerUUID) {
        Database db = dbSystem.get().getDatabase();
        PlayerContainer player = db.query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        return new PlayerPage(
                player,
                versionCheckSystem.get(),
                fileSystem.get(), config.get(), this, theme.get(),
                formatters.get(), serverInfo.get()
        );
    }

    public PlayerPluginTab inspectPluginTabs(UUID playerUUID) {
        Database database = dbSystem.get().getDatabase();

        Map<UUID, List<ExtensionData>> extensionPlayerData = database.query(new ExtensionPlayerDataQuery(playerUUID));

        if (extensionPlayerData.isEmpty()) {
            return new PlayerPluginTab("", Collections.emptyList(), formatters.get());
        }

        List<PlayerPluginTab> playerPluginTabs = new ArrayList<>();
        for (Map.Entry<UUID, Server> entry : database.query(ServerQueries.fetchPlanServerInformation()).entrySet()) {
            UUID serverUUID = entry.getKey();
            String serverName = entry.getValue().getIdentifiableName();

            List<ExtensionData> ofServer = extensionPlayerData.get(serverUUID);
            if (ofServer == null) {
                continue;
            }

            playerPluginTabs.add(new PlayerPluginTab(serverName, ofServer, formatters.get()));
        }

        StringBuilder navs = new StringBuilder();
        StringBuilder tabs = new StringBuilder();

        playerPluginTabs.stream().sorted().forEach(tab -> {
            navs.append(tab.getNav());
            tabs.append(tab.getTab());
        });

        return new PlayerPluginTab(navs.toString(), tabs.toString());
    }

    public NetworkPage networkPage() {
        return new NetworkPage(dbSystem.get(),
                versionCheckSystem.get(), fileSystem.get(), config.get(), theme.get(), serverInfo.get(), formatters.get());
    }
}