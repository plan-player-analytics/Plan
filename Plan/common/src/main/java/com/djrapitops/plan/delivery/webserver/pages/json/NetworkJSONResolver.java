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
package com.djrapitops.plan.delivery.webserver.pages.json;

import com.djrapitops.plan.delivery.rendering.json.JSONFactory;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkOverviewJSONCreator;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkPlayerBaseOverviewJSONCreator;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkSessionsOverviewJSONCreator;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkTabJSONCreator;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.pages.CompositePageResolver;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Resolves /v1/network/ JSON requests.
 *
 * @author Rsl1122
 */
@Singleton
public class NetworkJSONResolver extends CompositePageResolver {

    @Inject
    public NetworkJSONResolver(
            ResponseFactory responseFactory,
            JSONFactory jsonFactory,
            NetworkOverviewJSONCreator networkOverviewJSONCreator,
            NetworkPlayerBaseOverviewJSONCreator networkPlayerBaseOverviewJSONCreator,
            NetworkSessionsOverviewJSONCreator networkSessionsOverviewJSONCreator
    ) {
        super(responseFactory);

        registerPage("overview", DataID.SERVER_OVERVIEW, networkOverviewJSONCreator);
        registerPage("playerbaseOverview", DataID.PLAYERBASE_OVERVIEW, networkPlayerBaseOverviewJSONCreator);
        registerPage("sessionsOverview", DataID.SESSIONS_OVERVIEW, networkSessionsOverviewJSONCreator);
        registerPage("servers", DataID.SERVERS, jsonFactory::serversAsJSONMaps);
        registerPage("pingTable", DataID.PING_TABLE, jsonFactory::pingPerGeolocation);
    }

    private <T> void registerPage(String identifier, DataID dataID, NetworkTabJSONCreator<T> tabJSONCreator) {
        registerPage(identifier, new NetworkTabJSONResolver<>(dataID, tabJSONCreator));
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) {
        return true;
    }
}