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
package com.djrapitops.plan.delivery.webserver.resolver.json.token;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.web.resolver.AsyncResolver;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.MethodNotAllowedException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.auth.AuthTokenStore;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.webuser.DeleteApiTokenTransaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * DELETE /v1/deleteApiToken endpoint.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/deleteApiToken")
public class TokenDeleteJSONResolver implements AsyncResolver {

    private final DBSystem dbSystem;
    private final AuthTokenStore authTokenStore;

    @Inject
    public TokenDeleteJSONResolver(DBSystem dbSystem, AuthTokenStore authTokenStore) {
        this.dbSystem = dbSystem;
        this.authTokenStore = authTokenStore;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().map(user -> user.hasPermission(WebPermission.MANAGE_TOKENS)).orElse(false);
    }

    @DELETE
    @Operation(
            description = "Update token identifier and/or permissions",
            parameters = {
                    @Parameter(name = "token", description = "Existing token to delete", required = true),
            },
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON, examples = @ExampleObject("{\"success\": true}"))),
                    @ApiResponse(responseCode = "400", description = "No token given"),
                    @ApiResponse(responseCode = "405", description = "Not using DELETE method"),
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject("[\"page\"]")))
    )
    @Override
    public CompletableFuture<Optional<Response>> resolveAsync(Request request) {
        if (!"DELETE".equals(request.getMethod())) {
            throw new MethodNotAllowedException("DELETE");
        }

        @Untrusted String token = request.getQuery().get("token")
                .orElseThrow(() -> new BadRequestException("'token' parameter not given."));

        return dbSystem.getDatabase().executeTransaction(new DeleteApiTokenTransaction(token))
                .thenCompose(v -> authTokenStore.refreshTokens())
                .thenApply(v -> Optional.of(Response.builder()
                        .setStatus(200)
                        .setJSONContent("{\"success\": true}")
                        .build()));
    }
}
