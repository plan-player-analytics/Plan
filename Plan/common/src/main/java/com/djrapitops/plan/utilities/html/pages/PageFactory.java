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

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
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
import java.util.Map;
import java.util.UUID;

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
    private final Lazy<AnalysisContainer.Factory> analysisContainerFactory;
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
            Lazy<AnalysisContainer.Factory> analysisContainerFactory,
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
        this.analysisContainerFactory = analysisContainerFactory;
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

    public AnalysisPage analysisPage(UUID serverUUID) {
        AnalysisContainer analysisContainer = analysisContainerFactory.get()
                .forServerContainer(dbSystem.get().getDatabase().query(ContainerFetchQueries.fetchServerContainer(serverUUID)));
        return new AnalysisPage(analysisContainer, versionCheckSystem.get(), fileSystem.get(), formatters.get().decimals(), timings.get());
    }

    public InspectPage inspectPage(UUID uuid) {
        Database db = dbSystem.get().getDatabase();
        PlayerContainer player = db.query(ContainerFetchQueries.fetchPlayerContainer(uuid));
        Map<UUID, String> serverNames = db.fetch().getServerNames();
        return new InspectPage(
                player, serverNames,
                versionCheckSystem.get(),
                fileSystem.get(), config.get(), theme.get(),
                graphs.get(), tables.get(), accordions.get(), formatters.get(),
                serverInfo.get(), timings.get()
        );
    }

    public InspectPagePluginsContent inspectPagePluginsContent(UUID playerUUID) {
        return InspectPagePluginsContent.generateForThisServer(playerUUID, serverInfo.get(), hookHandler.get());
    }

    public NetworkPage networkPage() {
        NetworkContainer networkContainer = dbSystem.get().getDatabase()
                .query(ContainerFetchQueries.fetchNetworkContainer()); // Not cached, big.
        return new NetworkPage(networkContainer,
                analysisPluginsTabContentCreator.get(),
                versionCheckSystem.get(), fileSystem.get(), serverInfo.get().getServerProperties());
    }
}