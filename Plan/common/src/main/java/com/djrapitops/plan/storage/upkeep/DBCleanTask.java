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
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveUnsatisfiedConditionalPlayerResultsTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveUnsatisfiedConditionalServerResultsTransaction;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.transactions.commands.RemovePlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveDuplicateUserInfoTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveOldExtensionsTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveOldSampledDataTransaction;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

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
 * @author Rsl1122
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

                database.executeTransaction(new RemoveOldSampledDataTransaction(
                        serverInfo.getServerUUID(),
                        config.get(TimeSettings.DELETE_TPS_DATA_AFTER),
                        config.get(TimeSettings.DELETE_PING_DATA_AFTER)
                ));
                database.executeTransaction(new RemoveDuplicateUserInfoTransaction());
                database.executeTransaction(new RemoveUnsatisfiedConditionalPlayerResultsTransaction());
                database.executeTransaction(new RemoveUnsatisfiedConditionalServerResultsTransaction());
                int removed = cleanOldPlayers(database);
                if (removed > 0) {
                    logger.info(locale.getString(PluginLang.DB_NOTIFY_CLEAN, removed));
                }
                Long deleteExtensionDataAfter = config.get(TimeSettings.DELETE_EXTENSION_DATA_AFTER);
                if (System.currentTimeMillis() - lastReload <= deleteExtensionDataAfter) {
                    database.executeTransaction(new RemoveOldExtensionsTransaction(deleteExtensionDataAfter, serverInfo.getServerUUID()));
                }
            }
        } catch (DBOpException e) {
            errorLogger.log(L.ERROR, e);
            cancel();
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        AbsRunnable taskToRegister = this;
        // Secondary task for registration due to database queries.
        runnableFactory.create(null, new AbsRunnable() {
            @Override
            public void run() {
                // Distribute clean task evenly between multiple servers.
                // see https://github.com/plan-player-analytics/Plan/issues/1641 for why
                Integer biggestId = dbSystem.getDatabase().query(ServerQueries.fetchBiggestServerID());
                Integer id = serverInfo.getServer().getId().orElse(1);

                double distributor = id * 1.0 / biggestId; // 0 < distributor <= 1
                long distributingOverTime = config.get(TimeSettings.CLEAN_DATABASE_PERIOD);

                // -40 seconds to start first at 20 seconds if only one server is present.
                long startAfter = (long) (distributor * distributingOverTime) - 40L;

                long delay = TimeAmount.toTicks(startAfter, TimeUnit.MILLISECONDS);
                long period = TimeAmount.toTicks(config.get(TimeSettings.CLEAN_DATABASE_PERIOD), TimeUnit.MILLISECONDS);
                runnableFactory.create(null, taskToRegister).runTaskTimerAsynchronously(delay, period);
            }
        }).runTaskAsynchronously();
    }

    // VisibleForTesting
    public int cleanOldPlayers(Database database) {
        long now = System.currentTimeMillis();
        long keepActiveAfter = now - config.get(TimeSettings.DELETE_INACTIVE_PLAYERS_AFTER);

        List<UUID> inactivePlayers = database.query(fetchInactivePlayerUUIDs(keepActiveAfter));
        for (UUID playerUUID : inactivePlayers) {
            queryService.playerRemoved(playerUUID);
            database.executeTransaction(new RemovePlayerTransaction(playerUUID));
        }
        return inactivePlayers.size();
    }

    private Query<List<UUID>> fetchInactivePlayerUUIDs(long keepActiveAfter) {
        String sql = SELECT + "uuid, last_seen" + FROM +
                '(' + SELECT + "MAX(" + SessionsTable.SESSION_END + ") as last_seen, " +
                SessionsTable.USER_UUID +
                FROM + SessionsTable.TABLE_NAME +
                GROUP_BY + SessionsTable.USER_UUID + ") as q1" +
                WHERE + "last_seen < ?";
        return new QueryStatement<List<UUID>>(sql, 20000) {

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