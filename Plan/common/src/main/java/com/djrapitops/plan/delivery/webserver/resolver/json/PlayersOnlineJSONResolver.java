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

import com.djrapitops.plan.delivery.domain.PlayerIdentifier;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.ThemeDto;
import com.djrapitops.plan.delivery.rendering.json.JSONFactory;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.ServerUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Endpoint for getting players online at specific date
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/playersOnline")
public class PlayersOnlineJSONResolver implements Resolver {

    private final Identifiers identifiers;
    private final JSONFactory jsonFactory;

    @Inject
    public PlayersOnlineJSONResolver(Identifiers identifiers, JSONFactory jsonFactory) {

        this.identifiers = identifiers;
        this.jsonFactory = jsonFactory;
    }

    @Override
    public boolean canAccess(Request request) {
        boolean forServer = request.getQuery().get("server").isPresent();
        WebPermission permission = forServer ? WebPermission.PAGE_SERVER_OVERVIEW_PLAYERS_ONLINE_GRAPH
                : WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_ONLINE;

        return request.getUser().map(user -> user.hasPermission(permission)).orElse(false);
    }

    @GET
    @Operation(
            description = "Get theme json for a name",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON, schema = @Schema(implementation = ThemeDto.class))),
                    @ApiResponse(responseCode = "400", description = "If 'date' parameter is not specified or invalid")
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "date", description = "Epoch millisecond", required = true),
                    @Parameter(in = ParameterIn.QUERY, name = "server", description = "Server UUID")
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(Response.builder()
                .setJSONContent(getResponse(request))
                .build());
    }

    private List<PlayerIdentifier> getResponse(Request request) {
        URIQuery query = request.getQuery();
        try {
            Long date = query.get("date").map(Long::parseLong)
                    .orElseThrow(() -> new BadRequestException("Missing date"));
            if (query.get("server").isPresent()) {
                ServerUUID serverUUID = identifiers.getServerUUID(request);
                return jsonFactory.playersOnlineOn(date, serverUUID);
            } else {
                return jsonFactory.playersOnlineOn(date);
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid 'date'");
        }
    }
}
