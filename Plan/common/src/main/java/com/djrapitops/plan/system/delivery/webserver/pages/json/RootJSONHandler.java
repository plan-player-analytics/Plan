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
package com.djrapitops.plan.system.delivery.webserver.pages.json;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.system.delivery.webserver.RequestTarget;
import com.djrapitops.plan.system.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.system.delivery.webserver.pages.TreePageHandler;
import com.djrapitops.plan.system.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.system.identification.Identifiers;
import com.djrapitops.plan.system.json.*;

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

        registerPage("players", playersTableJSONHandler);
        registerPage("sessions", sessionsJSONHandler);
        registerPage("kills", playerKillsJSONHandler);
        registerPage("pingTable", jsonFactory::pingPerGeolocation);
        registerPage("graph", graphsJSONHandler);

        registerPage("serverOverview", serverOverviewJSONParser);
        registerPage("onlineOverview", onlineActivityOverviewJSONParser);
        registerPage("sessionsOverview", sessionsOverviewJSONParser);
        registerPage("playerVersus", pvPPvEJSONParser);
        registerPage("playerbaseOverview", playerBaseOverviewJSONParser);
        registerPage("performanceOverview", performanceJSONParser);

        registerPage("player", playerJSONHandler);
        registerPage("network", networkJSONHandler);
    }

    private <T> void registerPage(String identifier, ServerTabJSONParser<T> tabJSONParser) {
        registerPage(identifier, new ServerTabJSONHandler<>(identifiers, tabJSONParser));

    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return true;
    }
}