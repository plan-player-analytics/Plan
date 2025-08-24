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

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.ServerDto;
import com.djrapitops.plan.delivery.rendering.json.JSONFactory;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.utilities.dev.Untrusted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * @author AuroraLS3
 */
@Singleton
public class ServerIdentityJSONResolver implements Resolver {

    private final JSONFactory jsonFactory;

    @Inject
    public ServerIdentityJSONResolver(JSONFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        return user.hasPermission(WebPermission.ACCESS_SERVER);
    }

    @GET
    @Operation(
            description = "Get server identity for an identifier",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON, schema = @Schema(implementation = ServerDto.class))),
                    @ApiResponse(responseCode = "400", description = "If 'server' parameter is not given"),
                    @ApiResponse(responseCode = "404", description = "If 'server' parameter is not an existing server")
            },
            parameters = @Parameter(in = ParameterIn.QUERY, required = true, name = "server", description = "Server identifier to get data for", examples = {
                    @ExampleObject("Server 1"),
                    @ExampleObject("1"),
                    @ExampleObject("1fb39d2a-eb82-4868-b245-1fad17d823b3"),
            }),
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        @Untrusted String serverIdentifier = request.getQuery().get("server")
                .orElseThrow(() -> new BadRequestException("Missing 'server' query parameter"));
        ServerDto server = jsonFactory.serverForIdentifier(serverIdentifier)
                .orElseThrow(() -> new NotFoundException("Server with given identifier was not found in the database"));
        return Optional.of(Response.builder()
                .setJSONContent(server)
                .build());
    }
}
