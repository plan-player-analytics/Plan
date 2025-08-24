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
 * Resolves /v1/kills JSON requests.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/kills")
public class PlayerKillsJSONResolver extends JSONResolver {

    private final Identifiers identifiers;
    private final AsyncJSONResolverService jsonResolverService;
    private final JSONFactory jsonFactory;

    @Inject
    public PlayerKillsJSONResolver(
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
        return request.getUser().orElse(new WebUser("")).hasPermission(WebPermission.PAGE_SERVER_PLAYER_VERSUS_KILL_LIST);
    }

    @GET
    @Operation(
            description = "Get player kill data for a server",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON, examples = {
                            @ExampleObject("{\"player_kills\": []}")
                    })),
                    @ApiResponse(responseCode = "400 (no parameter)", description = "If 'server' parameter is not given"),
                    @ApiResponse(responseCode = "400 (no match)", description = "If 'server' parameter does not match an existing server")
            },
            parameters = @Parameter(in = ParameterIn.QUERY, name = "server", description = "Identifier for the server", examples = {
                    @ExampleObject("dade56b7-366a-495a-a087-5bf0178536d4"),
                    @ExampleObject("Server 1"),
                    @ExampleObject("1"),
            }),
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        ServerUUID serverUUID = identifiers.getServerUUID(request);
        Optional<Long> timestamp = Identifiers.getTimestamp(request);
        JSONStorage.StoredJSON storedJSON = jsonResolverService.resolve(timestamp, DataID.KILLS, serverUUID,
                theUUID -> Collections.singletonMap("player_kills", jsonFactory.serverPlayerKillsAsJSONMaps(theUUID))
        );
        return getCachedOrNewResponse(request, storedJSON);
    }
}