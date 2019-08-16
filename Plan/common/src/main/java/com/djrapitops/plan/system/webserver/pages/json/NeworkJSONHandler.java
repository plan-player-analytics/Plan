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
package com.djrapitops.plan.system.webserver.pages.json;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.system.json.network.NetworkOverviewJSONParser;
import com.djrapitops.plan.system.json.network.NetworkPlayerBaseOverviewJSONParser;
import com.djrapitops.plan.system.json.network.NetworkTabJSONParser;
import com.djrapitops.plan.system.webserver.RequestTarget;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.pages.TreePageHandler;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Root handler for different JSON end points.
 *
 * @author Rsl1122
 */
@Singleton
public class NeworkJSONHandler extends TreePageHandler {

    @Inject
    public NeworkJSONHandler(
            ResponseFactory responseFactory,
            NetworkOverviewJSONParser networkOverviewJSONParser,
            NetworkPlayerBaseOverviewJSONParser playerBaseOverviewJSONParser
    ) {
        super(responseFactory);

        registerPage("overview", networkOverviewJSONParser);
        registerPage("playerbaseOverview", playerBaseOverviewJSONParser);
    }

    private <T> void registerPage(String identifier, NetworkTabJSONParser<T> tabJSONParser) {
        registerPage(identifier, new NetworkTabJSONHandler<>(tabJSONParser));

    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return true;
    }
}