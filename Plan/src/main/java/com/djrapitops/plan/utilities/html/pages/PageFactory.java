package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
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
import javax.inject.Named;
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

    private final String version;
    private final Lazy<PlanFiles> fileSystem;
    private final Lazy<PlanConfig> config;
    private final Lazy<Theme> theme;
    private final Lazy<Database> database;
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
            @Named("currentVersion") String version,
            Lazy<PlanFiles> fileSystem,
            Lazy<PlanConfig> config,
            Lazy<Theme> theme,
            Lazy<Database> database,
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
        this.version = version;
        this.fileSystem = fileSystem;
        this.config = config;
        this.theme = theme;
        this.database = database;
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
                version,
                database.get(), serverInfo.get(), connectionSystem.get(), formatters.get(),
                debugLogger.get(), timings.get(), errorHandler.get()
        );
    }

    public PlayersPage playersPage() {
        return new PlayersPage(version, fileSystem.get(), config.get(),
                database.get(), serverInfo.get(), tables.get(),
                timings.get());
    }

    public AnalysisPage analysisPage(UUID serverUUID) {
        AnalysisContainer analysisContainer = analysisContainerFactory.get()
                .forServerContainer(database.get().fetch().getServerContainer(serverUUID));
        return new AnalysisPage(analysisContainer, fileSystem.get(), formatters.get().decimals(), timings.get());
    }

    public InspectPage inspectPage(UUID uuid) {
        PlayerContainer player = database.get().fetch().getPlayerContainer(uuid);
        Map<UUID, String> serverNames = database.get().fetch().getServerNames();
        return new InspectPage(
                player, serverNames,
                version,
                fileSystem.get(), config.get(), theme.get(),
                graphs.get(), tables.get(), accordions.get(), formatters.get(),
                serverInfo.get(), timings.get()
        );
    }

    public InspectPagePluginsContent inspectPagePluginsContent(UUID playerUUID) {
        return InspectPagePluginsContent.generateForThisServer(playerUUID, serverInfo.get(), hookHandler.get());
    }

    public NetworkPage networkPage() {
        NetworkContainer networkContainer = database.get().fetch().getNetworkContainer(); // Not cached, big.
        return new NetworkPage(networkContainer,
                analysisPluginsTabContentCreator.get(),
                fileSystem.get(), serverInfo.get().getServerProperties());
    }
}