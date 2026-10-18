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

import com.djrapitops.plan.delivery.domain.auth.ApiToken;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.web.resolver.AsyncResolver;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.MethodNotAllowedException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.auth.AuthTokenStore;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreApiTokenTransaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * POST /v1/saveApiToken endpoint.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/saveApiToken")
public class TokenSaveJSONResolver implements AsyncResolver {

    private final DBSystem dbSystem;
    private final AuthTokenStore authTokenStore;
    private final SecureRandom secureRandom = new SecureRandom();

    @Inject
    public TokenSaveJSONResolver(DBSystem dbSystem, AuthTokenStore authTokenStore) {
        this.dbSystem = dbSystem;
        this.authTokenStore = authTokenStore;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().map(user -> user.hasPermission(WebPermission.MANAGE_TOKENS)).orElse(false);
    }

    private String generateToken() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return DigestUtils.sha256Hex(new String(bytes, StandardCharsets.UTF_8)).substring(0, 20);
    }

    @POST
    @Operation(
            description = "Update token identifier and/or permissions",
            parameters = {
                    @Parameter(name = "identifier", description = "Identifier to call this token", required = true),
                    @Parameter(name = "token", description = "Existing token to update (optional) - new generated if not given"),
            },
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON, examples = @ExampleObject("{\"success\": true}"))),
                    @ApiResponse(responseCode = "400", description = "No identifier given"),
                    @ApiResponse(responseCode = "405", description = "Not using POST method"),
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject("[\"page\"]")))
    )
    @Override
    public CompletableFuture<Optional<Response>> resolveAsync(Request request) {
        if (!"POST".equals(request.getMethod())) {
            throw new MethodNotAllowedException("POST");
        }

        @Untrusted String identifier = request.getQuery().get("identifier")
                .orElseThrow(() -> new BadRequestException("'identifier' parameter not given."));
        @Untrusted String token = request.getQuery().get("token").orElseGet(this::generateToken);

        return request.getRequestBodyAsync()
                .thenApply(requestBody -> new String(requestBody, StandardCharsets.UTF_8))
                .thenApply(requestBody -> Arrays.asList(new Gson().fromJson(requestBody, String[].class)))
                .thenCompose(permissions -> dbSystem.getDatabase().executeTransaction(new StoreApiTokenTransaction(
                        new ApiToken(null, identifier, token, System.currentTimeMillis(), null),
                        permissions
                )))
                .thenCompose(v -> authTokenStore.refreshTokens())
                .thenApply(v -> Optional.of(Response.builder()
                        .setStatus(200)
                        .setJSONContent("{\"success\": true}")
                        .build()));
    }
}
