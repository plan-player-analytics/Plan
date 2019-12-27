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
import com.djrapitops.plan.delivery.webserver.pages.CompositePageResolver;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.identification.Identifiers;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Root resolver for JSON requests, resolves /v1/ URLs.
 *
 * @author Rsl1122
 */
@Singleton
public class RootJSONResolver extends CompositePageResolver {

    private final Identifiers identifiers;

    @Inject
    public RootJSONResolver(
            ResponseFactory responseFactory,
            Identifiers identifiers,
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

            PlayerJSONResolver playerJSONResolver,
            NetworkJSONResolver networkJSONResolver
    ) {
        super(responseFactory);
        this.identifiers = identifiers;

        registerPage("players", playersTableJSONResolver, 1);
        registerPage("sessions", sessionsJSONResolver, 0);
        registerPage("kills", playerKillsJSONResolver, 0);
        registerPage("pingTable", DataID.PING_TABLE, jsonFactory::pingPerGeolocation);
        registerPage("graph", graphsJSONResolver, 0);

        registerPage("serverOverview", DataID.SERVER_OVERVIEW, serverOverviewJSONCreator);
        registerPage("onlineOverview", DataID.ONLINE_OVERVIEW, onlineActivityOverviewJSONCreator);
        registerPage("sessionsOverview", DataID.SESSIONS_OVERVIEW, sessionsOverviewJSONCreator);
        registerPage("playerVersus", DataID.PVP_PVE, pvPPvEJSONCreator);
        registerPage("playerbaseOverview", DataID.PLAYERBASE_OVERVIEW, playerBaseOverviewJSONCreator);
        registerPage("performanceOverview", DataID.PERFORMANCE_OVERVIEW, performanceJSONCreator);

        registerPage("player", playerJSONResolver, 2);
        registerPage("network", networkJSONResolver, 0);
    }

    private <T> void registerPage(String identifier, DataID dataID, ServerTabJSONCreator<T> tabJSONCreator) {
        registerPage(identifier, new ServerTabJSONResolver<>(dataID, identifiers, tabJSONCreator), 0);
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) {
        return true;
    }
}