package com.djrapitops.plan.system.database;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.database.databases.sql.SpongeMySQLDB;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * DBSystem for Sponge.
 *
 * @author Rsl1122
 */
@Singleton
public class SpongeDBSystem extends DBSystem {

    private final PlanConfig config;

    @Inject
    public SpongeDBSystem(
            Locale locale,
            SpongeMySQLDB spongeMySQLDB,
            SQLiteDB.Factory sqLiteDB,
            PlanConfig config,
            PluginLogger logger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        super(locale, sqLiteDB, logger, timings, errorHandler);
        this.config = config;

        databases.add(spongeMySQLDB);
        databases.add(sqLiteDB.usingDefaultFile());
    }

    @Override
    public void enable() throws EnableException {
        String dbType = config.getString(Settings.DB_TYPE).toLowerCase().trim();
        db = getActiveDatabaseByName(dbType);
        super.enable();
    }
}