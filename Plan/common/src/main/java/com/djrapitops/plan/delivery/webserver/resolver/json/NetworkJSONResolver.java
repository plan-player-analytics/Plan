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

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.rendering.json.JSONFactory;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkOverviewJSONCreator;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkPlayerBaseOverviewJSONCreator;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkSessionsOverviewJSONCreator;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkTabJSONCreator;
import com.djrapitops.plan.delivery.web.resolver.CompositeResolver;
import com.djrapitops.plan.delivery.webserver.cache.AsyncJSONResolverService;
import com.djrapitops.plan.delivery.webserver.cache.DataID;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Resolves /v1/network/ JSON requests.
 *
 * @author AuroraLS3
 */
@Singleton
public class NetworkJSONResolver {

    private final AsyncJSONResolverService asyncJSONResolverService;
    private final CompositeResolver resolver;

    @Inject
    public NetworkJSONResolver(
            AsyncJSONResolverService asyncJSONResolverService, JSONFactory jsonFactory,
            NetworkOverviewJSONCreator networkOverviewJSONCreator,
            NetworkPlayerBaseOverviewJSONCreator networkPlayerBaseOverviewJSONCreator,
            NetworkSessionsOverviewJSONCreator networkSessionsOverviewJSONCreator,
            NetworkPerformanceJSONResolver networkPerformanceJSONResolver
    ) {
        this.asyncJSONResolverService = asyncJSONResolverService;
        resolver = CompositeResolver.builder()
                .add("overview", forJSON(DataID.SERVER_OVERVIEW, networkOverviewJSONCreator, WebPermission.PAGE_NETWORK_OVERVIEW_NUMBERS))
                .add("playerbaseOverview", forJSON(DataID.PLAYERBASE_OVERVIEW, networkPlayerBaseOverviewJSONCreator, WebPermission.PAGE_NETWORK_PLAYERBASE_OVERVIEW))
                .add("sessionsOverview", forJSON(DataID.SESSIONS_OVERVIEW, networkSessionsOverviewJSONCreator, WebPermission.PAGE_NETWORK_SESSIONS_OVERVIEW))
                .add("servers", forJSON(DataID.SERVERS, jsonFactory::serversAsJSONMaps, WebPermission.PAGE_NETWORK_SERVER_LIST))
                .add("pingTable", forJSON(DataID.PING_TABLE, jsonFactory::pingPerGeolocation, WebPermission.PAGE_NETWORK_GEOLOCATIONS_PING_PER_COUNTRY))
                .add("listServers", forJSON(DataID.LIST_SERVERS, jsonFactory::listServers, WebPermission.PAGE_NETWORK_PERFORMANCE))
                .add("serverOptions", forJSON(DataID.LIST_SERVERS, jsonFactory::listServers, WebPermission.PAGE_NETWORK_PERFORMANCE))
                .add("performanceOverview", networkPerformanceJSONResolver)
                .build();
    }

    private <T> NetworkTabJSONResolver<T> forJSON(DataID dataID, NetworkTabJSONCreator<T> tabJSONCreator, WebPermission permission) {
        return new NetworkTabJSONResolver<>(dataID, permission, tabJSONCreator, asyncJSONResolverService);
    }

    public CompositeResolver getResolver() {
        return resolver;
    }
}