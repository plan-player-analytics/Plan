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

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionGraphDto;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.cache.AsyncJSONResolverService;
import com.djrapitops.plan.delivery.webserver.resolver.ETag;
import com.djrapitops.plan.extension.implementation.storage.queries.graph.ExtensionGraphQueries;
import com.djrapitops.plan.extension.implementation.storage.queries.graph.ExtensionPlayerGraphQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.graph.ExtensionServerGraphQuery;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
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
import java.util.Optional;
import java.util.UUID;

/**
 * Extension data from /v1/extensionGraph for player/server/network.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/extensionGraph")
public class ExtensionGraphJSONResolver implements Resolver {

    private final DBSystem dbSystem;
    private final Identifiers identifiers;

    @Inject
    public ExtensionGraphJSONResolver(DBSystem dbSystem, Identifiers identifiers, AsyncJSONResolverService jsonResolverService) {
        this.dbSystem = dbSystem;
        this.identifiers = identifiers;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        if (request.getQuery().contains("player")) return user.hasPermission(WebPermission.PAGE_PLAYER_PLUGINS);
        return user.hasPermission(WebPermission.PAGE_NETWORK_PLUGINS) || user.hasPermission(WebPermission.PAGE_SERVER_PLUGINS);
    }

    @GET
    @Operation(
            description = "Get extension data of a specific server.",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON)),
                    @ApiResponse(responseCode = "400", description = "If 'server'/'graph parameter is not given"),
                    @ApiResponse(responseCode = "404", description = "If 'server'/'graph'/'player' parameter is not an existing server/graph/player")
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, required = true, name = "graph", description = "Name of the graph - get names from /v1/extensionData", examples = {
                            @ExampleObject("plan_extension_graph_pluginname_graphname"),
                    }),
                    @Parameter(in = ParameterIn.QUERY, required = true, name = "server", description = "Server identifier to get data for", examples = {
                            @ExampleObject("Server 1"),
                            @ExampleObject("1"),
                            @ExampleObject("1fb39d2a-eb82-4868-b245-1fad17d823b3"),
                    }),
                    @Parameter(in = ParameterIn.QUERY, name = "player", description = "Player identifier to get data for", examples = {
                            @ExampleObject("AuroraLS3"),
                            @ExampleObject("1fb39d2a-eb82-4868-b245-1fad17d823b3"),
                    })
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        @Untrusted URIQuery query = request.getQuery();

        @Untrusted String identifier = query.get("server")
                .orElseThrow(() -> new BadRequestException("'server' parameter was not given"));
        ServerUUID serverUUID = identifiers.getServerUUID(identifier)
                .orElseThrow(() -> new NotFoundException("Server with given server-parameter was not found in database"));

        String graph = identifiers.getGraphName(query); // Can throw BadRequestException

        @Untrusted Optional<ETag> tag = Identifiers.getEtag(request);
        Long etag = tag.map(eTag -> eTag.parseAsLong()
                        .orElseThrow(() -> new BadRequestException("If-Modified-Since should be a 64bit number")))
                .orElse(null);
        if (query.contains("player")) {
            UUID playerUUID = identifiers.getPlayerUUID(request); // Can throw BadRequestException
            return getPlayerResponse(serverUUID, playerUUID, graph, etag);
        } else {
            return getServerResponse(serverUUID, graph, etag);
        }
    }

    private Response getServerResponse(ServerUUID serverUUID, String graph, Long etag) {
        long lastModified = getLastModified(serverUUID, graph);
        if (etag != null && etag == lastModified) {
            return Response.builder()
                    .setStatus(304)
                    .setContent(new byte[0])
                    .build();
        }
        ExtensionGraphDto data = dbSystem.getDatabase().query(new ExtensionServerGraphQuery(serverUUID, graph))
                .orElseThrow(() -> new NotFoundException("Graph not found"));
        return Response.builder()
                .setStatus(200)
                .setJSONContent(data)
                .build();
    }

    private Response getPlayerResponse(ServerUUID serverUUID, UUID playerUUID, String graph, Long etag) {
        long lastModified = getLastModified(serverUUID, playerUUID, graph);
        if (etag != null && etag == lastModified) {
            return Response.builder()
                    .setStatus(304)
                    .setContent(new byte[0])
                    .build();
        }

        ExtensionGraphDto data = dbSystem.getDatabase().query(new ExtensionPlayerGraphQuery(serverUUID, playerUUID, graph))
                .orElseThrow(() -> new NotFoundException("Graph not found"));
        return Response.builder()
                .setStatus(200)
                .setJSONContent(data)
                .build();
    }

    private long getLastModified(ServerUUID serverUUID, UUID playerUUID, String graph) {
        return dbSystem.getDatabase().query(ExtensionGraphQueries.findLastModified(serverUUID, playerUUID, graph))
                .orElse(0L);
    }

    private long getLastModified(ServerUUID serverUUID, String graph) {
        return dbSystem.getDatabase().query(ExtensionGraphQueries.findLastModified(serverUUID, graph))
                .orElse(0L);
    }
}
