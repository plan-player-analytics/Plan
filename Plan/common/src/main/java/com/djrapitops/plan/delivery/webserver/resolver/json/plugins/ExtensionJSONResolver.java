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
import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionDataDto;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.cache.AsyncJSONResolverService;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.delivery.webserver.resolver.json.JSONResolver;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author AuroraLS3
 */
@Singleton
public class ExtensionJSONResolver extends JSONResolver {

    private final DBSystem dbSystem;
    private final Identifiers identifiers;
    private final AsyncJSONResolverService jsonResolverService;

    @Inject
    public ExtensionJSONResolver(DBSystem dbSystem, Identifiers identifiers, AsyncJSONResolverService jsonResolverService) {
        this.dbSystem = dbSystem;
        this.identifiers = identifiers;
        this.jsonResolverService = jsonResolverService;
    }

    @Override
    public Formatter<Long> getHttpLastModifiedFormatter() {return jsonResolverService.getHttpLastModifiedFormatter();}

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        return user.hasPermission(WebPermission.PAGE_NETWORK_PLUGINS) || user.hasPermission(WebPermission.PAGE_SERVER_PLUGINS);
    }

    @GET
    @Operation(
            description = "Get extension data of a specific server.",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON)),
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
        @Untrusted String identifier = request.getQuery().get("server")
                .orElseThrow(() -> new BadRequestException("'server' parameter was not given"));
        ServerUUID serverUUID = identifiers.getServerUUID(identifier)
                .orElseThrow(() -> new NotFoundException("Server with given server-parameter was not found in database"));
        return Optional.of(getResponse(request, serverUUID));
    }

    private JSONStorage.StoredJSON getJSON(@Untrusted Request request, ServerUUID serverUUID) {
        Optional<Long> timestamp = Identifiers.getTimestamp(request);

        return jsonResolverService.resolve(
                timestamp, DataID.EXTENSION_JSON, serverUUID,
                this::getExtensionData
        );
    }

    private Response getResponse(Request request, ServerUUID serverUUID) {
        JSONStorage.StoredJSON json = getJSON(request, serverUUID);
        return getCachedOrNewResponse(request, json);
    }

    private Map<String, List<ExtensionDataDto>> getExtensionData(ServerUUID serverUUID) {
        List<ExtensionData> extensionData = dbSystem.getDatabase().query(new ExtensionServerDataQuery(serverUUID));
        return Map.of(
                "extensions", extensionData.stream()
                        .sorted()
                        .map(ExtensionDataDto::new)
                        .collect(Collectors.toList())
        );
    }
}
