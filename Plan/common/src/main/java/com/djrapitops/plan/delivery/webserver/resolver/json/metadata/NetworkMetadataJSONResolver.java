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

import com.djrapitops.plan.delivery.domain.datatransfer.ServerDto;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.java.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/networkMetadata")
public class NetworkMetadataJSONResolver implements NoAuthResolver {

    private final ServerInfo serverInfo;
    private final ServerSensor<?> serverSensor;
    private final DBSystem dbSystem;

    @Inject
    public NetworkMetadataJSONResolver(ServerInfo serverInfo, ServerSensor<?> serverSensor, DBSystem dbSystem) {
        this.serverInfo = serverInfo;
        this.serverSensor = serverSensor;
        this.dbSystem = dbSystem;
    }

    @GET
    @Operation(
            description = "Get metadata about the network such as list of servers.",
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse());
    }

    private Response getResponse() {
        return Response.builder()
                .setJSONContent(Maps.builder(String.class, Object.class)
                        .put("servers", dbSystem.getDatabase().query(ServerQueries.fetchPlanServerInformationCollection())
                                .stream().map(ServerDto::fromServer)
                                .sorted()
                                .collect(Collectors.toList()))
                        .put("currentServer", ServerDto.fromServer(serverInfo.getServer()))
                        .put("usingRedisBungee", serverSensor.usingRedisBungee())
                        .build())
                .build();
    }
}
