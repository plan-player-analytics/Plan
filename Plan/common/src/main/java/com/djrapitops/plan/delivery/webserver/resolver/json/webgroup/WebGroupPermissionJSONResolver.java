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
import com.djrapitops.plan.delivery.domain.auth.WebPermissionList;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Endpoint for getting list of Plan web permissions of specific group.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/groupPermissions")
public class WebGroupPermissionJSONResolver implements Resolver {

    private final DBSystem dbSystem;

    @Inject
    public WebGroupPermissionJSONResolver(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().map(user -> user.hasPermission(WebPermission.MANAGE_GROUPS)).orElse(false);
    }

    @GET
    @Operation(
            description = "Get list of web permissions that have been granted to specific group",
            parameters = {
                    @Parameter(name = "group", description = "Name of the group", required = true),
            },
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(
                            mediaType = MimeType.JSON,
                            schema = @Schema(implementation = WebPermissionList.class))),
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(@Untrusted Request request) {
        @Untrusted String group = request.getQuery().get("group")
                .orElseThrow(() -> new BadRequestException("Parameter 'group' not given."));
        List<String> permissions = dbSystem.getDatabase().query(WebUserQueries.fetchGroupPermissions(group));

        WebPermissionList permissionList = new WebPermissionList(permissions);
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(permissionList)
                .build();
    }
}
