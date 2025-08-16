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

import com.djrapitops.plan.delivery.domain.datatransfer.preferences.Preferences;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.MethodNotAllowedException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebUserPreferencesTransaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/storePreferences")
public class StorePreferencesJSONResolver implements Resolver {

    private final DBSystem dbSystem;

    @Inject
    public StorePreferencesJSONResolver(DBSystem dbSystem) {this.dbSystem = dbSystem;}

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().isPresent();
    }

    @POST
    @Operation(
            description = "Update user preferences",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Storage was successful"),
                    @ApiResponse(responseCode = "400", description = "Request body does not match json format of preferences"),
                    @ApiResponse(responseCode = "403", description = "Not logged in (This endpoint only accepts requests if logged in)"),
                    @ApiResponse(responseCode = "405", description = "Not POST request"),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = Preferences.class)))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        if (!"POST".equals(request.getMethod())) {
            throw new MethodNotAllowedException("POST");
        }
        WebUser user = request.getUser()
                .orElseThrow(() -> new ForbiddenException("This endpoint only accepts requests if logged in."));
        String preferencesBody = new String(request.getRequestBody(), StandardCharsets.UTF_8);
        try {
            Gson gson = new Gson();
            @Untrusted String syntaxSanitized = gson.toJson(gson.fromJson(preferencesBody, Preferences.class));
            dbSystem.getDatabase().executeTransaction(new StoreWebUserPreferencesTransaction(syntaxSanitized, user));
        } catch (JsonSyntaxException invalidSyntax) {
            throw new BadRequestException("Request body does not match json format of preferences");
        }
        return Optional.empty();
    }
}
