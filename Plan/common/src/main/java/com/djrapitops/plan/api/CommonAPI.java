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
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.identification.UUIDUtility;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PlanAPI extension for all implementations.
 *
 * @author AuroraLS3
 * @deprecated Plan API v4 has been deprecated, use the APIv5 instead (<a href="https://github.com/plan-player-analytics/Plan/wiki/APIv5">wiki</a>).
 */
@Singleton
@Deprecated(forRemoval = true, since = "5.0")
public class CommonAPI implements PlanAPI {

    private final DBSystem dbSystem;
    private final UUIDUtility uuidUtility;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public CommonAPI(
            DBSystem dbSystem,
            UUIDUtility uuidUtility,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.dbSystem = dbSystem;
        this.uuidUtility = uuidUtility;
        this.logger = logger;
        this.errorLogger = errorLogger;
        PlanAPIHolder.set(this);
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        logger.warn(pluginData.getClass().getName() + " was attempted to be registered." +
                " PluginData API has been decommissioned, so this is a no-op." +
                " Please move to using DataExtension API. https://github.com/plan-player-analytics/Plan/wiki/APIv5");
    }

    @Override
    public String getPlayerInspectPageLink(UUID uuid) {
        return getPlayerInspectPageLink(getPlayerName(uuid));
    }

    @Override
    public String getPlayerInspectPageLink(String playerName) {
        return "../player/" + Html.encodeToURL(playerName);
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
            errorLogger.error(e);
            return new HashMap<>();
        }
    }

    @Override
    public PlayerContainer fetchPlayerContainer(UUID uuid) {
        return new PlayerContainer(queryDB(ContainerFetchQueries.fetchPlayerContainer(uuid)));
    }

    @Override
    public ServerContainer fetchServerContainer(UUID serverUUID) {
        return new ServerContainer();
    }

    @Override
    public Collection<UUID> fetchServerUUIDs() {
        return queryDB(ServerQueries.fetchPlanServerInformation()).keySet()
                .stream().map(ServerUUID::asUUID).collect(Collectors.toSet());
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        return queryDB(UserIdentifierQueries.fetchPlayerNameOf(playerUUID)).orElse(null);
    }

    private <T> T queryDB(Query<T> query) {
        return dbSystem.getDatabase().query(query);
    }
}
