package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Factory for creating different {@link Page} objects.
 *
 * @author Rsl1122
 */
@Singleton
public class PageFactory {

    private final PlanPlugin plugin;
    private final String version;
    private final PlanConfig config;
    private final Database database;
    private final ServerInfo serverInfo;
    private final ConnectionSystem connectionSystem;
    private final DebugLogger debugLogger;
    private final Timings timings;
    private final ErrorHandler errorHandler;

    @Inject
    public PageFactory(
            PlanPlugin plugin,
            @Named("currentVersion") String version,
            PlanConfig config,
            Database database,
            ServerInfo serverInfo,
            ConnectionSystem connectionSystem,
            DebugLogger debugLogger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        this.plugin = plugin;
        this.version = version;
        this.config = config;
        this.database = database;
        this.serverInfo = serverInfo;
        this.connectionSystem = connectionSystem;
        this.debugLogger = debugLogger;
        this.timings = timings;
        this.errorHandler = errorHandler;
    }

    public DebugPage debugPage() {
        return new DebugPage(version, database, serverInfo, connectionSystem, debugLogger, timings, errorHandler);
    }

    public PlayersPage playersPage() {
        return new PlayersPage(version, config, database, serverInfo, timings);
    }

}