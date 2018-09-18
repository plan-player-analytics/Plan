package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
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
    private final Lazy<PlanConfig> config;
    private final Lazy<Database> database;
    private final Lazy<ServerInfo> serverInfo;
    private final Lazy<ConnectionSystem> connectionSystem;
    private final Lazy<Graphs> graphs;
    private final Lazy<HtmlTables> tables;
    private final Lazy<Formatters> formatters;
    private final Lazy<DebugLogger> debugLogger;
    private final Lazy<Timings> timings;
    private final Lazy<ErrorHandler> errorHandler;

    @Inject
    public PageFactory(
            @Named("currentVersion") String version,
            Lazy<PlanConfig> config,
            Lazy<Database> database,
            Lazy<ServerInfo> serverInfo,
            Lazy<ConnectionSystem> connectionSystem,
            Lazy<Graphs> graphs,
            Lazy<HtmlTables> tables,
            Lazy<Formatters> formatters,
            Lazy<DebugLogger> debugLogger,
            Lazy<Timings> timings,
            Lazy<ErrorHandler> errorHandler
    ) {
        this.version = version;
        this.config = config;
        this.database = database;
        this.serverInfo = serverInfo;
        this.connectionSystem = connectionSystem;
        this.graphs = graphs;
        this.tables = tables;
        this.formatters = formatters;
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
        return new PlayersPage(version, config.get(), database.get(), serverInfo.get(), tables.get(), timings.get());
    }

    public AnalysisPage analysisPage(UUID serverUUID) {
        AnalysisContainer analysisContainer = new AnalysisContainer(database.get().fetch().getServerContainer(serverUUID));
        return new AnalysisPage(analysisContainer, formatters.get().decimals());
    }

    public InspectPage inspectPage(UUID uuid) {
        PlayerContainer player = database.get().fetch().getPlayerContainer(uuid);
        Map<UUID, String> serverNames = database.get().fetch().getServerNames();
        return new InspectPage(
                player, serverNames,
                config.get(), graphs.get(), tables.get(), formatters.get(), serverInfo.get(), timings.get()
        );
    }

    public NetworkPage networkPage() {
        NetworkContainer networkContainer = database.get().fetch().getNetworkContainer(); // Not cached, big.
        return new NetworkPage(networkContainer, serverInfo.get().getServerProperties());
    }
}