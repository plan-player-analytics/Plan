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

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.rendering.json.JSONFactory;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.cache.AsyncJSONResolverService;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.utilities.dev.Untrusted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Optional;

/**
 * Resolves /v1/sessions JSON requests.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/sessions")
public class SessionsJSONResolver extends JSONResolver {

    private final Identifiers identifiers;
    private final AsyncJSONResolverService jsonResolverService;
    private final JSONFactory jsonFactory;

    @Inject
    public SessionsJSONResolver(
            Identifiers identifiers,
            AsyncJSONResolverService jsonResolverService,
            JSONFactory jsonFactory
    ) {
        this.identifiers = identifiers;
        this.jsonResolverService = jsonResolverService;
        this.jsonFactory = jsonFactory;
    }

    @Override
    public Formatter<Long> getHttpLastModifiedFormatter() {return jsonResolverService.getHttpLastModifiedFormatter();}

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        if (request.getQuery().get("server").isPresent()) {
            return user.hasPermission(WebPermission.PAGE_SERVER_SESSIONS_LIST);
        }
        return user.hasPermission(WebPermission.PAGE_NETWORK_SESSIONS_LIST);
    }

    @GET
    @Operation(
            description = "Get sessions for a server or whole network",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON)),
                    @ApiResponse(responseCode = "400", description = "If 'server' parameter is not an existing server")
            },
            parameters = @Parameter(in = ParameterIn.QUERY, name = "server", description = "Server identifier to get data for (optional)", examples = {
                    @ExampleObject("Server 1"),
                    @ExampleObject("1"),
                    @ExampleObject("1fb39d2a-eb82-4868-b245-1fad17d823b3"),
            }),
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        JSONStorage.StoredJSON result = getStoredJSON(request);
        return getCachedOrNewResponse(request, result);
    }

    private JSONStorage.StoredJSON getStoredJSON(@Untrusted Request request) {
        Optional<Long> timestamp = Identifiers.getTimestamp(request);
        if (request.getQuery().get("server").isPresent()) {
            ServerUUID serverUUID = identifiers.getServerUUID(request);
            return jsonResolverService.resolve(timestamp, DataID.SESSIONS, serverUUID,
                    theUUID -> Collections.singletonMap("sessions", jsonFactory.serverSessionsAsJSONMap(theUUID))
            );
        }
        // Assume network
        return jsonResolverService.resolve(timestamp, DataID.SESSIONS,
                () -> Collections.singletonMap("sessions", jsonFactory.networkSessionsAsJSONMap())
        );
    }
}