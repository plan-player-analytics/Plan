package com.djrapitops.plan.db.tasks;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.transactions.init.CleanTransaction;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Task for cleaning the active database.
 *
 * @author Rsl1122
 */
@Singleton
public class DBCleanTask extends AbsRunnable {

    private final Locale locale;
    private final DBSystem dbSystem;
    private final PlanConfig config;
    private final ServerInfo serverInfo;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public DBCleanTask(
            PlanConfig config,
            Locale locale,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.locale = locale;

        this.dbSystem = dbSystem;
        this.config = config;
        this.serverInfo = serverInfo;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        Database database = dbSystem.getDatabase();
        try {
            if (database.getState() != Database.State.CLOSED) {
                database.executeTransaction(new CleanTransaction(serverInfo.getServerUUID(),
                        config.get(TimeSettings.KEEP_INACTIVE_PLAYERS), logger, locale)
                );
            }
        } catch (DBOpException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
            cancel();
        }
    }
}