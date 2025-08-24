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
package com.djrapitops.plan.delivery.webserver.resolver.json.plugins;

import com.djrapitops.plan.delivery.domain.PluginHistoryMetadata;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.PluginHistoryDto;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.PluginMetadataQueries;
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
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Endpoint for getting plugin version history.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/pluginHistory")
public class PluginHistoryJSONResolver implements Resolver {

    private final DBSystem dbSystem;
    private final Identifiers identifiers;

    @Inject
    public PluginHistoryJSONResolver(DBSystem dbSystem, Identifiers identifiers) {
        this.dbSystem = dbSystem;
        this.identifiers = identifiers;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser()
                .map(user -> user.hasPermission(WebPermission.PAGE_NETWORK_PLUGIN_HISTORY)
                        || user.hasPermission(WebPermission.PAGE_SERVER_PLUGIN_HISTORY))
                .orElse(false);
    }

    @Override
    @Operation(
            description = "Get plugin history for a server since installation of Plan.",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON,
                            schema = @Schema(implementation = PluginHistoryDto.class))),
            },
            parameters = @Parameter(in = ParameterIn.QUERY, name = "server", description = "Server identifier to get data for (optional)", examples = {
                    @ExampleObject("Server 1"),
                    @ExampleObject("1"),
                    @ExampleObject("1fb39d2a-eb82-4868-b245-1fad17d823b3"),
            }),
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @GET
    public Optional<Response> resolve(@Untrusted Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(@Untrusted Request request) {
        ServerUUID serverUUID = identifiers.getServerUUID(request); // Can throw BadRequestException
        List<PluginHistoryMetadata> history = dbSystem.getDatabase().query(PluginMetadataQueries.getPluginHistory(serverUUID));
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(new PluginHistoryDto(history))
                .build();
    }
}
