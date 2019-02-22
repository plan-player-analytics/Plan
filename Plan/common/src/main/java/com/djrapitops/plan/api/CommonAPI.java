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
package com.djrapitops.plan.api;

import com.djrapitops.plan.api.data.PlayerContainer;
import com.djrapitops.plan.api.data.ServerContainer;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.db.access.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.database.databases.sql.operation.SQLFetchOps;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlanAPI extension for all implementations.
 *
 * @author Rsl1122
 */
@Singleton
public class CommonAPI implements PlanAPI {

    private final DBSystem dbSystem;
    private final UUIDUtility uuidUtility;
    private final HookHandler hookHandler;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public CommonAPI(
            DBSystem dbSystem,
            UUIDUtility uuidUtility,
            HookHandler hookHandler,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.dbSystem = dbSystem;
        this.uuidUtility = uuidUtility;
        this.hookHandler = hookHandler;
        this.logger = logger;
        this.errorHandler = errorHandler;
        PlanAPIHolder.set(this);
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        hookHandler.addPluginDataSource(pluginData);
    }

    @Override
    public String getPlayerInspectPageLink(UUID uuid) {
        return getPlayerInspectPageLink(getPlayerName(uuid));
    }

    @Override
    public String getPlayerInspectPageLink(String playerName) {
        return "../player/" + playerName;
    }

    @Override
    public UUID playerNameToUUID(String playerName) {
        return uuidUtility.getUUIDOf(playerName);
    }

    @Override
    public Map<UUID, String> getKnownPlayerNames() {
        try {
            return queryDB(UserIdentifierQueries.fetchAllPlayerNames());
        } catch (DBOpException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
            return new HashMap<>();
        }
    }

    @Override
    public PlayerContainer fetchPlayerContainer(UUID uuid) {
        return new PlayerContainer(queryDB(ContainerFetchQueries.fetchPlayerContainer(uuid)));
    }

    @Override
    public ServerContainer fetchServerContainer(UUID serverUUID) {
        return new ServerContainer(queryDB(ContainerFetchQueries.fetchServerContainer(serverUUID)));
    }

    @Override
    public Collection<UUID> fetchServerUUIDs() {
        return queryDB(ServerQueries.fetchPlanServerInformation()).keySet();
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        return queryDB(UserIdentifierQueries.fetchPlayerNameOf(playerUUID)).orElse(null);
    }

    @Override
    public FetchOperations fetchFromPlanDB() {
        logger.warn("PlanAPI#fetchFromPlanDB has been deprecated and will be removed in the future. Stack trace to follow");
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            logger.warn(element.toString());
        }
        return new SQLFetchOps(dbSystem.getDatabase());
    }

    private <T> T queryDB(Query<T> query) {
        return dbSystem.getDatabase().query(query);
    }
}
