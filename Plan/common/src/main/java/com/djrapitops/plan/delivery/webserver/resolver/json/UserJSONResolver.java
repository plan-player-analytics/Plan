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

import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.WebServer;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves requests for /v1/user
 *
 * @author Kopo942
 */
@Singleton
public class UserJSONResolver implements Resolver {

    private final Lazy<WebServer> webServer;

    @Inject
    public UserJSONResolver(Lazy<WebServer> webServer) {
        this.webServer = webServer;
    }

    @Override
    public boolean canAccess(Request request) {
        return true;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        if (!webServer.get().isAuthRequired()) {
            return Response.builder()
                    .setStatus(404)
                    .setJSONContent("{}")
                    .build();
        }

        WebUser user = request.getUser().orElseThrow(() -> new WebUserAuthException(FailReason.NO_USER_PRESENT));
        Map<String, Object> json = new HashMap<>();

        json.put("username", user.getUsername());
        json.put("linkedTo", user.getName());
        json.put("linkedToUUID", user.getUUID().isPresent() ? user.getUUID().get().toString() : "");
        json.put("permissions", user.getPermissions());

        return Response.builder().setJSONContent(json).build();
    }
}
