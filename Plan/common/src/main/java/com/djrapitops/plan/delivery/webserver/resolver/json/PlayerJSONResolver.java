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
import com.djrapitops.plan.delivery.rendering.json.PlayerJSONCreator;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.identification.Identifiers;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Singleton
@Path("/v1/player")
public class PlayerJSONResolver implements Resolver {

    private final Identifiers identifiers;
    private final PlayerJSONCreator jsonCreator;

    @Inject
    public PlayerJSONResolver(Identifiers identifiers, PlayerJSONCreator jsonCreator) {
        this.identifiers = identifiers;
        this.jsonCreator = jsonCreator;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        if (user.hasPermission(WebPermission.ACCESS_PLAYER)) return true;
        if (user.hasPermission(WebPermission.ACCESS_PLAYER_SELF)) {
            try {
                UUID webUserUUID = identifiers.getPlayerUUID(user.getName());
                UUID playerUUID = identifiers.getPlayerUUID(request);
                return playerUUID.equals(webUserUUID);
            } catch (BadRequestException userDoesntExist) {
                return false; // Don't give away who has played on the server to someone with level 2 access
            }
        }
        return false;
    }

    @GET
    @Operation(
            description = "Get player data for visualizing a single player",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON)),
                    @ApiResponse(responseCode = "400", description = "If 'player' parameter is not given")
            },
            parameters = @Parameter(in = ParameterIn.QUERY, name = "player", description = "Identifier for the player", examples = {
                    @ExampleObject("dade56b7-366a-495a-a087-5bf0178536d4"),
                    @ExampleObject("AuroraLS3"),
            }),
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        UUID playerUUID = identifiers.getPlayerUUID(request); // Can throw BadRequestException

        Predicate<WebPermission> hasPermission = request.getUser()
                .map(user -> (Predicate<WebPermission>) user::hasPermission)
                .orElse(permission -> true); // No user means auth disabled inside resolve
        Map<String, Object> jsonAsMap = jsonCreator.createJSONAsMap(playerUUID, hasPermission);
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(jsonAsMap)
                .build();
    }
}
