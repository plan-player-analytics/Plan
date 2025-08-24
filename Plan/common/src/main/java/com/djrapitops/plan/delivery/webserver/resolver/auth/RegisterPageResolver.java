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
package com.djrapitops.plan.delivery.webserver.resolver.auth;

import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.utilities.dev.Untrusted;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class RegisterPageResolver implements NoAuthResolver {

    private final ResponseFactory responseFactory;
    private final Lazy<WebServer> webServer;

    @Inject
    public RegisterPageResolver(
            ResponseFactory responseFactory,
            Lazy<WebServer> webServer
    ) {
        this.responseFactory = responseFactory;
        this.webServer = webServer;
    }

    @Override
    public Optional<Response> resolve(@Untrusted Request request) {
        Optional<WebUser> user = request.getUser();
        if (user.isPresent() || !webServer.get().isAuthRequired()) {
            return Optional.of(responseFactory.redirectResponse("/"));
        }
        return Optional.of(responseFactory.reactPageResponse(request));
    }
}
