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
import com.djrapitops.plan.delivery.rendering.json.network.NetworkOverviewJSONParser;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkPlayerBaseOverviewJSONParser;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkSessionsOverviewJSONParser;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkTabJSONParser;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.pages.TreePageHandler;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.exceptions.WebUserAuthException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Root handler for different JSON end points.
 *
 * @author Rsl1122
 */
@Singleton
public class NetworkJSONHandler extends TreePageHandler {

    @Inject
    public NetworkJSONHandler(
            ResponseFactory responseFactory,
            JSONFactory jsonFactory,
            NetworkOverviewJSONParser networkOverviewJSONParser,
            NetworkPlayerBaseOverviewJSONParser playerBaseOverviewJSONParser,
            NetworkSessionsOverviewJSONParser sessionsOverviewJSONParser
    ) {
        super(responseFactory);

        registerPage("overview", DataID.SERVER_OVERVIEW, networkOverviewJSONParser);
        registerPage("playerbaseOverview", DataID.PLAYERBASE_OVERVIEW, playerBaseOverviewJSONParser);
        registerPage("sessionsOverview", DataID.SESSIONS_OVERVIEW, sessionsOverviewJSONParser);
        registerPage("servers", DataID.SERVERS, jsonFactory::serversAsJSONMaps);
        registerPage("pingTable", DataID.PING_TABLE, jsonFactory::pingPerGeolocation);
    }

    private <T> void registerPage(String identifier, DataID dataID, NetworkTabJSONParser<T> tabJSONParser) {
        registerPage(identifier, new NetworkTabJSONHandler<>(dataID, tabJSONParser));
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return true;
    }
}