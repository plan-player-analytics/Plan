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
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.cache.AsyncJSONResolverService;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.AllowlistQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
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
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

/**
 * Response resolver to get game allowlist bounces.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/gameAllowlistBounces")
public class AllowlistJSONResolver extends JSONResolver {

    private final DBSystem dbSystem;
    private final Identifiers identifiers;
    private final AsyncJSONResolverService jsonResolverService;

    @Inject
    public AllowlistJSONResolver(DBSystem dbSystem, Identifiers identifiers, AsyncJSONResolverService jsonResolverService) {
        this.dbSystem = dbSystem;
        this.identifiers = identifiers;
        this.jsonResolverService = jsonResolverService;
    }

    @Override
    public Formatter<Long> getHttpLastModifiedFormatter() {return jsonResolverService.getHttpLastModifiedFormatter();}

    @Override
    public boolean canAccess(@Untrusted Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));

        return user.hasPermission(WebPermission.PAGE_SERVER_ALLOWLIST_BOUNCE);
    }

    @GET
    @Operation(
            description = "Get allowlist bounce data for server",
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
    public Optional<Response> resolve(@Untrusted Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(@Untrusted Request request) {
        JSONStorage.StoredJSON result = getStoredJSON(request);
        return getCachedOrNewResponse(request, result);
    }

    @Nullable
    private JSONStorage.StoredJSON getStoredJSON(Request request) {
        Optional<Long> timestamp = Identifiers.getTimestamp(request);

        ServerUUID serverUUID = identifiers.getServerUUID(request);
        Database database = dbSystem.getDatabase();
        return jsonResolverService.resolve(timestamp, DataID.PLAYER_ALLOWLIST_BOUNCES, serverUUID,
                theUUID -> Map.of(
                        "allowlist_bounces", database.query(AllowlistQueries.getBounces(serverUUID)),
                        "last_seen_by_uuid", database.query(SessionQueries.lastSeen(serverUUID))
                )
        );
    }

}
