/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database;

import com.djrapitops.plan.system.database.databases.sql.MySQLDB;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Bungee Database system that initializes MySQL object.
 *
 * @author Rsl1122
 */
@Singleton
public class ProxyDBSystem extends DBSystem {

    @Inject
    public ProxyDBSystem(Locale locale, MySQLDB mySQLDB,
                         PluginLogger logger, Timings timings, ErrorHandler errorHandler) {
        super(locale, logger, timings, errorHandler);
        databases.add(mySQLDB);
        db = mySQLDB;
    }
}
