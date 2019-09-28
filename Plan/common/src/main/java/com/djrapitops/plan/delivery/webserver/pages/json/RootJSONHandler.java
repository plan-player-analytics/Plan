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

import com.djrapitops.plan.delivery.rendering.json.*;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.pages.TreePageHandler;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.identification.Identifiers;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Root handler for different JSON end points.
 *
 * @author Rsl1122
 */
@Singleton
public class RootJSONHandler extends TreePageHandler {

    private Identifiers identifiers;

    @Inject
    public RootJSONHandler(
            ResponseFactory responseFactory,
            Identifiers identifiers,
            JSONFactory jsonFactory,

            GraphsJSONHandler graphsJSONHandler,
            SessionsJSONHandler sessionsJSONHandler,
            PlayersTableJSONHandler playersTableJSONHandler,
            ServerOverviewJSONParser serverOverviewJSONParser,
            OnlineActivityOverviewJSONParser onlineActivityOverviewJSONParser,
            SessionsOverviewJSONParser sessionsOverviewJSONParser,
            PlayerKillsJSONHandler playerKillsJSONHandler,
            PvPPvEJSONParser pvPPvEJSONParser,
            PlayerBaseOverviewJSONParser playerBaseOverviewJSONParser,
            PerformanceJSONParser performanceJSONParser,

            PlayerJSONHandler playerJSONHandler,
            NetworkJSONHandler networkJSONHandler
    ) {
        super(responseFactory);
        this.identifiers = identifiers;

        registerPage("players", playersTableJSONHandler, 1);
        registerPage("sessions", sessionsJSONHandler, 0);
        registerPage("kills", playerKillsJSONHandler, 0);
        registerPage("pingTable", DataID.PING_TABLE, jsonFactory::pingPerGeolocation);
        registerPage("graph", graphsJSONHandler, 0);

        registerPage("serverOverview", DataID.SERVER_OVERVIEW, serverOverviewJSONParser);
        registerPage("onlineOverview", DataID.ONLINE_OVERVIEW, onlineActivityOverviewJSONParser);
        registerPage("sessionsOverview", DataID.SESSIONS_OVERVIEW, sessionsOverviewJSONParser);
        registerPage("playerVersus", DataID.PVP_PVE, pvPPvEJSONParser);
        registerPage("playerbaseOverview", DataID.PLAYERBASE_OVERVIEW, playerBaseOverviewJSONParser);
        registerPage("performanceOverview", DataID.PERFORMANCE_OVERVIEW, performanceJSONParser);

        registerPage("player", playerJSONHandler, 2);
        registerPage("network", networkJSONHandler, 0);
    }

    private <T> void registerPage(String identifier, DataID dataID, ServerTabJSONParser<T> tabJSONParser) {
        registerPage(identifier, new ServerTabJSONHandler<>(dataID, identifiers, tabJSONParser), 0);
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return true;
    }
}