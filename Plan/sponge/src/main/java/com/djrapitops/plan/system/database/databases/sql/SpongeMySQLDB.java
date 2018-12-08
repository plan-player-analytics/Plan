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
package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Lazy;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * MySQLDB implementation for Sponge since default driver is not available.
 *
 * @author Rsl1122
 */
public class SpongeMySQLDB extends MySQLDB {

    @Inject
    public SpongeMySQLDB(
            Locale locale,
            PlanConfig config,
            Lazy<ServerInfo> serverInfo,
            NetworkContainer.Factory networkContainerFactory,
            RunnableFactory runnableFactory,
            PluginLogger pluginLogger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        super(locale, config, serverInfo, networkContainerFactory, runnableFactory, pluginLogger, timings, errorHandler);
    }

    @Override
    public void setupDataSource() throws DBInitException {
        Optional<SqlService> sqlServiceProvider = Sponge.getServiceManager().provide(SqlService.class);
        if (!sqlServiceProvider.isPresent()) {
            return;
        }

        String host = config.getString(DatabaseSettings.MYSQL_HOST);
        String port = config.getString(DatabaseSettings.MYSQL_PORT);
        String database = config.getString(DatabaseSettings.MYSQL_DATABASE);
        String launchOptions = config.getString(DatabaseSettings.MYSQL_LAUNCH_OPTIONS);
        if (launchOptions.isEmpty() || !launchOptions.startsWith("?") || launchOptions.endsWith("&")) {
            logger.error("Launch Options were faulty, using default (?rewriteBatchedStatements=true&useSSL=false)");
            launchOptions = "?rewriteBatchedStatements=true&useSSL=false";
        }

        String url = host + ":" + port + "/" + database + launchOptions;
        String username = config.getString(DatabaseSettings.MYSQL_USER);
        String password = config.getString(DatabaseSettings.MYSQL_PASS);
        try {
            this.dataSource = sqlServiceProvider.get().getDataSource(
                    "jdbc:mysql://" + username + ":" + password + "@" + url
            );
        } catch (SQLException e) {
            throw new DBInitException(e);
        }
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        try {
            return super.getConnection();
        } catch (SQLException e) {
            if (e.getMessage().contains("has been closed")) {
                try {
                    setupDataSource();
                } catch (DBInitException setupException) {
                    throw new IllegalStateException("Failed to set up a new datasource after connection failure.", setupException);
                }
                return super.getConnection();
            } else {
                throw e;
            }
        }
    }
}