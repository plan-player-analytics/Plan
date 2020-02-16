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
import com.djrapitops.plan.delivery.webserver.RequestInternal;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.delivery.webserver.pages.PageResolver;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.Identifiers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.UUID;

/**
 * Resolves /v1/sessions JSON requests.
 *
 * @author Rsl1122
 */
@Singleton
public class SessionsJSONResolver implements PageResolver {

    private final Identifiers identifiers;
    private final JSONFactory jsonFactory;

    @Inject
    public SessionsJSONResolver(
            Identifiers identifiers,
            JSONFactory jsonFactory
    ) {
        this.identifiers = identifiers;
        this.jsonFactory = jsonFactory;
    }

    @Override
    public Response_old resolve(RequestInternal request, RequestTarget target) throws WebException {
        if (target.getParameter("server").isPresent()) {
            UUID serverUUID = identifiers.getServerUUID(target);
            return JSONCache.getOrCache(DataID.SESSIONS, serverUUID, () ->
                    new JSONResponse(Collections.singletonMap("sessions", jsonFactory.serverSessionsAsJSONMap(serverUUID)))
            );
        }
        // Assume network
        return JSONCache.getOrCache(DataID.SESSIONS, () ->
                new JSONResponse(Collections.singletonMap("sessions", jsonFactory.networkSessionsAsJSONMap()))
        );
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}