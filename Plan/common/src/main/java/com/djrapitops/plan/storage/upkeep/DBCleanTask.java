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
package com.djrapitops.plan.storage.upkeep;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.transactions.commands.RemovePlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveDuplicateUserInfoTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveOldAccessLogTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveOldExtensionsTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveOldSampledDataTransaction;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.PluginRunnable;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Task for cleaning the active database.
 *
 * @author AuroraLS3
 */
@Singleton
public class DBCleanTask extends TaskSystem.Task {

    private final Locale locale;
    private final DBSystem dbSystem;
    private final PlanConfig config;
    private final QuerySvc queryService;
    private final ServerInfo serverInfo;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    // This variable assumes that the system is thrown away on reload and new one is constructed.
    // It is to avoid cleaning extension data that has not been updated after uptime longer than the deletion threshold.
    private final long lastReload;

    @Inject
    public DBCleanTask(
            PlanConfig config,
            Locale locale,
            DBSystem dbSystem,
            QuerySvc queryService,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.locale = locale;

        this.dbSystem = dbSystem;
        this.config = config;
        this.queryService = queryService;
        this.serverInfo = serverInfo;
        this.logger = logger;
        this.errorLogger = errorLogger;

        lastReload = System.currentTimeMillis();
    }

    @Override
    public void run() {
        Database database = dbSystem.getDatabase();
        try {
            if (database.getState() != Database.State.CLOSED) {

                database.executeTransaction(new RemoveOldAccessLogTransaction(TimeUnit.DAYS.toMillis(config.get(WebserverSettings.REMOVE_ACCESS_LOG_AFTER_DAYS))));
                database.executeTransaction(new RemoveOldSampledDataTransaction(
                        serverInfo.getServerUUID(),
                        config.get(TimeSettings.DELETE_TPS_DATA_AFTER),
                        config.get(TimeSettings.DELETE_PING_DATA_AFTER)
                ));
                database.executeTransaction(new RemoveDuplicateUserInfoTransaction());
                int removed = cleanOldPlayers(database);
                if (removed > 0) {
                    logger.info(locale.getString(PluginLang.DB_NOTIFY_CLEAN, removed));
                }
                Long deleteExtensionDataAfter = config.get(TimeSettings.DELETE_EXTENSION_DATA_AFTER);
                Long databaseCleanPeriod = config.get(TimeSettings.CLEAN_DATABASE_PERIOD);
                if (databaseCleanPeriod > deleteExtensionDataAfter) {
                    logger.warn("Data of Disabled Extensions can not be cleaned due to " + TimeSettings.CLEAN_DATABASE_PERIOD.getPath() + " being larger than " + TimeSettings.DELETE_EXTENSION_DATA_AFTER.getPath());
                }

                // Avoid cleaning extension data that has not been updated after uptime longer than the deletion threshold.
                // This is needed since the last updated number is updated at reload and it would lead to all data
                // for plugins being deleted all the time.
                if (System.currentTimeMillis() - lastReload <= deleteExtensionDataAfter) {
                    database.executeTransaction(new RemoveOldExtensionsTransaction(config.getExtensionSettings(), deleteExtensionDataAfter, serverInfo.getServerUUID()));
                }
            }
        } catch (DBOpException e) {
            errorLogger.error(e);
            cancel();
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        PluginRunnable taskToRegister = this;
        // Secondary task for registration due to database queries.
        runnableFactory.create(() -> {
            // Distribute clean task evenly between multiple servers.
            // see https://github.com/plan-player-analytics/Plan/issues/1641 for why
            Integer biggestId = dbSystem.getDatabase().query(ServerQueries.fetchBiggestServerID());
            Integer id = serverInfo.getServer().getId().orElse(1);

            double distributor = id * 1.0 / biggestId; // 0 < distributor <= 1
            long distributingOverTimeMs = config.get(TimeSettings.CLEAN_DATABASE_PERIOD);

            // -40 seconds to start first at 20 seconds if only one server is present and period is 1 minute.
            long startAfterMs = (long) (distributor * distributingOverTimeMs) - TimeUnit.SECONDS.toMillis(40L);

            long delayTicks = TimeAmount.toTicks(startAfterMs, TimeUnit.MILLISECONDS);
            long periodTicks = TimeAmount.toTicks(config.get(TimeSettings.CLEAN_DATABASE_PERIOD), TimeUnit.MILLISECONDS);
            runnableFactory.create(taskToRegister).runTaskTimerAsynchronously(delayTicks, periodTicks);
        }).runTaskAsynchronously();
    }

    // VisibleForTesting
    public int cleanOldPlayers(Database database) {
        // Only clean if this is a proxy server or no proxy servers are installed.
        if (serverInfo.getServer().isProxy() || database.query(ServerQueries.fetchProxyServers()).isEmpty()) {
            long now = System.currentTimeMillis();
            long keepActiveAfter = now - config.get(TimeSettings.DELETE_INACTIVE_PLAYERS_AFTER);

            List<UUID> inactivePlayers = database.query(fetchInactivePlayerUUIDs(keepActiveAfter));
            for (UUID playerUUID : inactivePlayers) {
                queryService.playerRemoved(playerUUID);
                database.executeTransaction(new RemovePlayerTransaction(playerUUID));
            }
            return inactivePlayers.size();
        }

        // Skip cleaning on game servers if proxy server is installed.
        return 0;
    }

    private Query<List<UUID>> fetchInactivePlayerUUIDs(long keepActiveAfter) {
        String selectLastSeen = SELECT + "MAX(" + SessionsTable.SESSION_END + ") as last_seen, " +
                SessionsTable.USER_ID +
                FROM + SessionsTable.TABLE_NAME +
                GROUP_BY + SessionsTable.USER_ID;
        String sql = SELECT + "uuid, last_seen" +
                FROM + '(' + selectLastSeen + ") as q1" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + '=' + "q1." + SessionsTable.USER_ID +
                WHERE + "last_seen < ?";
        return new QueryStatement<>(sql, 20000) {

            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, keepActiveAfter);
            }

            @Override
            public List<UUID> processResults(ResultSet set) throws SQLException {
                List<UUID> inactiveUUIDs = new ArrayList<>();
                while (set.next()) {
                    inactiveUUIDs.add(UUID.fromString(set.getString("uuid")));
                }
                return inactiveUUIDs;
            }
        };
    }
}