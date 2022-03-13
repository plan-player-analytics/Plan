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

import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.WebServer;
import com.djrapitops.plan.utilities.java.Maps;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class WhoAmIJSONResolver implements NoAuthResolver {

    private final Lazy<WebServer> webServer;

    @Inject
    public WhoAmIJSONResolver(Lazy<WebServer> webServer) {
        this.webServer = webServer;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        Optional<WebUser> foundUser = request.getUser();
        if (!foundUser.isPresent()) {
            return Response.builder()
                    .setJSONContent(Maps.builder(String.class, Boolean.class)
                            .put("authRequired", webServer.get().isAuthRequired())
                            .put("loggedIn", false)
                            .build())
                    .build();
        }

        return Response.builder()
                .setJSONContent(Maps.builder(String.class, Object.class)
                        .put("authRequired", webServer.get().isAuthRequired())
                        .put("loggedIn", true)
                        .put("user", foundUser.get())
                        .build())
                .build();
    }
}
