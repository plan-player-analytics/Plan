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
package com.djrapitops.plan.delivery.webserver.resolver.json.webgroup;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.MethodNotAllowedException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieStore;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.webuser.DeleteWebGroupTransaction;
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
import java.util.concurrent.ExecutionException;

/**
 * Endpoint for adding a group.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/deleteGroup")
public class WebGroupDeleteJSONResolver implements Resolver {

    private final DBSystem dbSystem;
    private final ActiveCookieStore activeCookieStore;

    @Inject
    public WebGroupDeleteJSONResolver(DBSystem dbSystem, ActiveCookieStore activeCookieStore) {
        this.dbSystem = dbSystem;
        this.activeCookieStore = activeCookieStore;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().map(user -> user.hasPermission(WebPermission.MANAGE_GROUPS)).orElse(false);
    }

    @DELETE
    @Operation(
            description = "Delete a group",
            parameters = {
                    @Parameter(name = "group", description = "Name of the group to delete", required = true),
                    @Parameter(name = "moveTo", description = "Name of the group to move users of deleted group to", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON,
                            examples = @ExampleObject("{\"success\": true}"))),
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        if (!"DELETE".equals(request.getMethod())) {
            throw new MethodNotAllowedException("DELETE");
        }
        @Untrusted String groupName = request.getQuery().get("group")
                .orElseThrow(() -> new BadRequestException("'group' parameter not given."));
        @Untrusted String moveTo = request.getQuery().get("moveTo")
                .orElseThrow(() -> new BadRequestException("'moveTo' parameter not given."));

        return Optional.of(getResponse(groupName, moveTo));
    }

    private Response getResponse(@Untrusted String groupName, @Untrusted String moveTo) {
        try {
            dbSystem.getDatabase().executeTransaction(new DeleteWebGroupTransaction(groupName, moveTo))
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
        activeCookieStore.reloadActiveCookies();

        return Response.builder()
                .setStatus(200)
                .setJSONContent("{\"success\": true}")
                .build();
    }
}
