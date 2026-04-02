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
package com.djrapitops.plan.delivery.webserver.resolver.json.query;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
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
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/datapoint")
public class DataPointJSONResolver implements Resolver {

    private final DatapointStore datapointStore;
    private final Formatter<Long> httpLastModifiedFormatter;


    @Inject
    public DataPointJSONResolver(DatapointStore datapointStore, Formatters formatters) {
        this.datapointStore = datapointStore;
        this.httpLastModifiedFormatter = formatters.httpLastModifiedLong();
    }

    @Override
    public boolean canAccess(Request request) {
        Optional<WebUser> user = request.getUser();
        if (user.isEmpty()) return false;
        DatapointType type = request.getQuery().get("type", DatapointType::find)
                .orElseThrow(() -> new BadRequestException("type is required"));
        GenericFilter filter = GenericFilter.of(request.getQuery());

        Optional<WebPermission> permission = datapointStore.getPermission(type, filter);
        if (permission.isEmpty()) return false;
        boolean hasPermission = user.get().hasPermission(permission.get());
        if (!hasPermission) return false;

        Optional<UUID> playerUUID = filter.getPlayerUUID();
        boolean isPlayerPermission = playerUUID.isPresent();
        if (isPlayerPermission) {
            if (user.get().hasPermission(WebPermission.ACCESS_PLAYER)) return true;

            boolean isSamePlayer = user.get().getUUID()
                    .filter(userUUID -> playerUUID.get().equals(userUUID))
                    .isPresent();
            return user.get().hasPermission(WebPermission.ACCESS_PLAYER_SELF) && isSamePlayer;
        }

        return true;
    }

    @GET
    @Operation(
            description = "Get datapoint value for player, server or network",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON)),
                    @ApiResponse(responseCode = "400", description = "If 'type' parameter is not set or supported")
            },
            parameters = @Parameter(in = ParameterIn.QUERY, name = "type", description = "Type of datapoint", examples = {
                    @ExampleObject("PLAYTIME"),
            }),
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        DatapointType type = request.getQuery().get("type", DatapointType::find)
                .orElseThrow(() -> new BadRequestException("type is required"));
        GenericFilter filter = GenericFilter.of(request.getQuery());
        @Untrusted Optional<ETag> tag = Identifiers.getEtag(request);
        Long etag = tag.map(eTag -> eTag.parseAsLong()
                        .orElseThrow(() -> new BadRequestException("If-Modified-Since should be a 64bit number")))
                .orElse(null);
        long lastModified = datapointStore.getLastModified(etag, type, filter);
        if (etag != null && etag == lastModified) {
            return Optional.of(Response.builder()
                    .setStatus(304)
                    .setContent(new byte[0])
                    .build());
        }
        return datapointStore.getValue(type, filter)
                .map(value -> newResponseBuilder()
                        .setJSONContent(value)
                        .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CHECK_ETAG)
                        .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                        .setHeader(HttpHeader.ETAG.asString(), lastModified)
                        .build());
    }
}
