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
package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.api.exceptions.connection.NotFoundException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.extension.implementation.results.player.ExtensionPlayerData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.response.pages.parts.InspectPagePluginsContent;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.structure.Accordions;
import com.djrapitops.plan.utilities.html.structure.AnalysisPluginsTabContentCreator;
import com.djrapitops.plan.utilities.html.tables.HtmlTables;
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
    private final Lazy<ConnectionSystem> connectionSystem;
    private final Lazy<Graphs> graphs;
    private final Lazy<HtmlTables> tables;
    private final Lazy<Accordions> accordions;
    private final Lazy<Formatters> formatters;
    private final Lazy<AnalysisPluginsTabContentCreator> analysisPluginsTabContentCreator;
    private final Lazy<HookHandler> hookHandler;
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
            Lazy<ConnectionSystem> connectionSystem,
            Lazy<Graphs> graphs,
            Lazy<HtmlTables> tables,
            Lazy<Accordions> accordions,
            Lazy<Formatters> formatters,
            Lazy<AnalysisPluginsTabContentCreator> analysisPluginsTabContentCreator,
            Lazy<HookHandler> hookHandler,
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
        this.connectionSystem = connectionSystem;
        this.graphs = graphs;
        this.tables = tables;
        this.accordions = accordions;
        this.formatters = formatters;
        this.analysisPluginsTabContentCreator = analysisPluginsTabContentCreator;
        this.hookHandler = hookHandler;
        this.debugLogger = debugLogger;
        this.timings = timings;
        this.errorHandler = errorHandler;
    }

    public DebugPage debugPage() {
        return new DebugPage(
                dbSystem.get().getDatabase(), serverInfo.get(), connectionSystem.get(), formatters.get(),
                debugLogger.get(), timings.get(), errorHandler.get()
        );
    }

    public PlayersPage playersPage() {
        return new PlayersPage(versionCheckSystem.get(), fileSystem.get(), config.get(),
                dbSystem.get().getDatabase(), serverInfo.get(), tables.get(),
                timings.get());
    }

    public ServerPage serverPage(UUID serverUUID) throws NotFoundException {
        return dbSystem.get().getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID))
                .map(server -> new ServerPage(
                        server,
                        config.get(),
                        theme.get(),
                        connectionSystem.get(),
                        versionCheckSystem.get(),
                        fileSystem.get(),
                        formatters.get()
                )).orElseThrow(() -> new NotFoundException("Server not found in the database"));
    }

    public InspectPage inspectPage(UUID playerUUID) {
        Database db = dbSystem.get().getDatabase();
        PlayerContainer player = db.query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        Map<UUID, String> serverNames = db.query(ServerQueries.fetchServerNames());
        return new InspectPage(
                player, serverNames,
                versionCheckSystem.get(),
                fileSystem.get(), config.get(), this, theme.get(),
                graphs.get(), tables.get(), accordions.get(), formatters.get(),
                serverInfo.get(), timings.get()
        );
    }

    public InspectPluginTab inspectPluginTabs(UUID playerUUID) {
        Database database = dbSystem.get().getDatabase();

        Map<UUID, List<ExtensionPlayerData>> extensionPlayerData = database.query(new ExtensionPlayerDataQuery(playerUUID));

        if (extensionPlayerData.isEmpty()) {
            return new InspectPluginTab("No Extensions", Collections.emptyList(), formatters.get());
        }

        List<InspectPluginTab> inspectPluginTabs = new ArrayList<>();
        for (Map.Entry<UUID, Server> entry : database.query(ServerQueries.fetchPlanServerInformation()).entrySet()) {
            UUID serverUUID = entry.getKey();
            String serverName = entry.getValue().getIdentifiableName();

            List<ExtensionPlayerData> ofServer = extensionPlayerData.get(serverUUID);
            if (ofServer == null) {
                continue;
            }

            inspectPluginTabs.add(new InspectPluginTab(serverName, ofServer, formatters.get()));
        }

        StringBuilder navs = new StringBuilder();
        StringBuilder tabs = new StringBuilder();

        inspectPluginTabs.stream().sorted().forEach(tab -> {
            navs.append(tab.getNav());
            tabs.append(tab.getTab());
        });

        return new InspectPluginTab(navs.toString(), tabs.toString());
    }

    /**
     * @deprecated Marked for removal when the connection system will be removed.
     */
    @Deprecated
    public InspectPagePluginsContent inspectPagePluginsContent(UUID playerUUID) {
        return InspectPagePluginsContent.generateForThisServer(playerUUID, serverInfo.get(), hookHandler.get());
    }

    public NetworkPage networkPage() {
        NetworkContainer networkContainer = dbSystem.get().getDatabase()
                .query(ContainerFetchQueries.fetchNetworkContainer()); // Not cached, big.
        return new NetworkPage(networkContainer,
                analysisPluginsTabContentCreator.get(),
                versionCheckSystem.get(), fileSystem.get(), serverInfo.get().getServerProperties(), formatters.get());
    }
}