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
import com.djrapitops.plan.delivery.web.resolver.AsyncResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.MethodNotAllowedException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebUserPreferencesTransaction;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/storePreferences")
public class StorePreferencesJSONResolver implements AsyncResolver {

    private final DBSystem dbSystem;
    private final ResponseFactory responseFactory;

    @Inject
    public StorePreferencesJSONResolver(DBSystem dbSystem, ResponseFactory responseFactory) {
        this.dbSystem = dbSystem;
        this.responseFactory = responseFactory;
    }

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
    public CompletableFuture<Optional<Response>> resolveAsync(Request request) {
        if (!"POST".equals(request.getMethod())) {
            throw new MethodNotAllowedException("POST");
        }
        var optUser = request.getUser();
        if (optUser.isEmpty()) {
            return CompletableFuture.completedFuture(
                    Optional.of(responseFactory.forbidden403("This endpoint only accepts requests if logged in."))
            );
        }

        WebUser user = optUser.get();
        return request.getRequestBodyAsync()
                .thenApply(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .thenApply(asString -> {
                    try {
                        Gson gson = new Gson();
                        return gson.toJson(gson.fromJson(asString, Preferences.class));
                    } catch (JsonSyntaxException invalidSyntax) {
                        throw new BadRequestException("Request body does not match json format of preferences");
                    }
                })
                .thenCompose(syntaxSanitized -> {
                    return dbSystem.getDatabase().executeTransaction(new StoreWebUserPreferencesTransaction(syntaxSanitized, user));
                })
                .thenApply(v -> Optional.of(Response.builder()
                        .setStatus(200)
                        .setJSONContent("{\"success\":true}")
                        .build()));
    }
}
