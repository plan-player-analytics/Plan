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
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.json.JSONFactory;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointStore;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.CacheStrategy;
import com.djrapitops.plan.delivery.webserver.resolver.ETag;
import com.djrapitops.plan.identification.Identifiers;
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
import org.eclipse.jetty.http.HttpHeader;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves /v1/sessions JSON requests.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/sessions")
public class SessionsJSONResolver implements Resolver {

    private final Identifiers identifiers;
    private final JSONFactory jsonFactory;
    private final DatapointStore datapointStore;
    private final Formatter<Long> httpLastModifiedFormatter;

    @Inject
    public SessionsJSONResolver(
            Identifiers identifiers,
            JSONFactory jsonFactory,
            DatapointStore datapointStore,
            Formatters formatters
    ) {
        this.identifiers = identifiers;
        this.jsonFactory = jsonFactory;
        this.datapointStore = datapointStore;
        this.httpLastModifiedFormatter = formatters.httpLastModifiedLong();
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        if (request.getQuery().get("player").isPresent()) {
            return user.hasPermission(WebPermission.PAGE_PLAYER_SESSIONS);
        }
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
        GenericFilter filter = identifiers.genericFilter(request.getQuery());

        @Untrusted Optional<ETag> tag = Identifiers.getEtag(request);
        Long etag = tag.map(eTag -> eTag.parseAsLong()
                        .orElseThrow(() -> new BadRequestException("If-Modified-Since should be a 64bit number")))
                .orElse(null);
        long lastModified = datapointStore.getLastModified(etag, DatapointType.PLAYTIME, filter);
        if (etag != null && etag == lastModified) {
            return Response.builder()
                    .setStatus(304)
                    .setContent(new byte[0])
                    .build();
        }

        Optional<UUID> playerUUID = filter.getPlayerUUID();
        if (playerUUID.isPresent()) {
            return newResponseBuilder()
                    .setJSONContent(jsonFactory.playerSessions(filter))
                    .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CHECK_ETAG)
                    .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                    .setHeader(HttpHeader.ETAG.asString(), lastModified)
                    .build();
        }
        if (!filter.getServerUUIDs().isEmpty()) {
            return newResponseBuilder()
                    .setJSONContent(jsonFactory.serverSessions(filter))
                    .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CHECK_ETAG)
                    .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                    .setHeader(HttpHeader.ETAG.asString(), lastModified)
                    .build();
        } else {
            return newResponseBuilder()
                    .setJSONContent(jsonFactory.networkSessions(filter))
                    .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CHECK_ETAG)
                    .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                    .setHeader(HttpHeader.ETAG.asString(), lastModified)
                    .build();
        }
    }
}