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
package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.rendering.json.*;
import com.djrapitops.plan.delivery.web.resolver.CompositeResolver;
import com.djrapitops.plan.delivery.webserver.cache.AsyncJSONResolverService;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.identification.Identifiers;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Root resolver for JSON requests, resolves /v1/ URLs.
 *
 * @author AuroraLS3
 */
@Singleton
public class RootJSONResolver {

    private final Identifiers identifiers;
    private final AsyncJSONResolverService asyncJSONResolverService;
    private final CompositeResolver resolver;

    @Inject
    public RootJSONResolver(
            Identifiers identifiers,
            AsyncJSONResolverService asyncJSONResolverService,
            JSONFactory jsonFactory,

            GraphsJSONResolver graphsJSONResolver,
            SessionsJSONResolver sessionsJSONResolver,
            PlayersTableJSONResolver playersTableJSONResolver,
            ServerOverviewJSONCreator serverOverviewJSONCreator,
            OnlineActivityOverviewJSONCreator onlineActivityOverviewJSONCreator,
            SessionsOverviewJSONCreator sessionsOverviewJSONCreator,
            PlayerKillsJSONResolver playerKillsJSONResolver,
            PvPPvEJSONCreator pvPPvEJSONCreator,
            PlayerBaseOverviewJSONCreator playerBaseOverviewJSONCreator,
            PerformanceJSONCreator performanceJSONCreator,
            ErrorsJSONResolver errorsJSONResolver,
            LocaleJSONResolver localeJSONResolver,

            PlayerJSONResolver playerJSONResolver,
            NetworkJSONResolver networkJSONResolver,
            FiltersJSONResolver filtersJSONResolver,
            QueryJSONResolver queryJSONResolver,
            VersionJSONResolver versionJSONResolver,
            MetadataJSONResolver metadataJSONResolver,
            WhoAmIJSONResolver whoAmIJSONResolver
    ) {
        this.identifiers = identifiers;
        this.asyncJSONResolverService = asyncJSONResolverService;

        resolver = CompositeResolver.builder()
                .add("players", playersTableJSONResolver)
                .add("sessions", sessionsJSONResolver)
                .add("kills", playerKillsJSONResolver)
                .add("graph", graphsJSONResolver)
                .add("pingTable", forJSON(DataID.PING_TABLE, jsonFactory::pingPerGeolocation))
                .add("serverOverview", forJSON(DataID.SERVER_OVERVIEW, serverOverviewJSONCreator))
                .add("onlineOverview", forJSON(DataID.ONLINE_OVERVIEW, onlineActivityOverviewJSONCreator))
                .add("sessionsOverview", forJSON(DataID.SESSIONS_OVERVIEW, sessionsOverviewJSONCreator))
                .add("playerVersus", forJSON(DataID.PVP_PVE, pvPPvEJSONCreator))
                .add("playerbaseOverview", forJSON(DataID.PLAYERBASE_OVERVIEW, playerBaseOverviewJSONCreator))
                .add("performanceOverview", forJSON(DataID.PERFORMANCE_OVERVIEW, performanceJSONCreator))
                .add("player", playerJSONResolver)
                .add("network", networkJSONResolver.getResolver())
                .add("filters", filtersJSONResolver)
                .add("query", queryJSONResolver)
                .add("errors", errorsJSONResolver)
                .add("version", versionJSONResolver)
                .add("locale", localeJSONResolver)
                .add("metadata", metadataJSONResolver)
                .add("whoami", whoAmIJSONResolver)
                .build();
    }

    private <T> ServerTabJSONResolver<T> forJSON(DataID dataID, ServerTabJSONCreator<T> tabJSONCreator) {
        return new ServerTabJSONResolver<>(dataID, identifiers, tabJSONCreator, asyncJSONResolverService);
    }

    public CompositeResolver getResolver() {
        return resolver;
    }
}