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
package com.djrapitops.plan.delivery.webserver.resolver.json.metadata;

import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.utilities.java.Maps;
import dagger.Lazy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@Path("/v1/whoami")
public class WhoAmIJSONResolver implements NoAuthResolver {

    private final Lazy<WebServer> webServer;

    @Inject
    public WhoAmIJSONResolver(Lazy<WebServer> webServer) {
        this.webServer = webServer;
    }

    @GET
    @Operation(
            description = "Get information about the currently logged in user",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON, examples = {
                            @ExampleObject(value = "{\"authRequired\": false, \"loggedIn\": false}", description = "Authentication is disabled"),
                            @ExampleObject(value = "{\"authRequired\": true, \"loggedIn\": false}", description = "Not logged in"),
                            @ExampleObject(value = "{\"authRequired\": true, \"loggedIn\": true, \"user\": {}}", description = "Logged in as user"),
                    })),
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        Optional<WebUser> foundUser = request.getUser();
        if (foundUser.isEmpty()) {
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
