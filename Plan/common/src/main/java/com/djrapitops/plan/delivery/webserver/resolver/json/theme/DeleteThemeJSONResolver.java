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
package com.djrapitops.plan.delivery.webserver.resolver.json.theme;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.MethodNotAllowedException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.dev.Untrusted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Endpoint to store themes from theme editor.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/deleteTheme")
public class DeleteThemeJSONResolver implements Resolver {

    private static final Pattern themeFilePattern = Pattern.compile("[a-zA-Z0-9-]*");

    private final PlanFiles files;
    private final ResponseFactory responseFactory;

    @Inject
    public DeleteThemeJSONResolver(PlanFiles files, ResponseFactory responseFactory) {
        this.files = files;
        this.responseFactory = responseFactory;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        return user.hasPermission(WebPermission.MANAGE_THEMES);
    }

    @POST
    @Operation(
            description = "Delete theme json with a name",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON)),
                    @ApiResponse(responseCode = "400", description = "If 'theme' parameter is not specified or invalid"),
            },
            parameters = @Parameter(in = ParameterIn.QUERY, name = "theme", description = "Name of the theme, alphanumeric with dashes", examples = {
                    @ExampleObject("default"),
                    @ExampleObject("color-blind")
            }),
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        if (!"DELETE".equals(request.getMethod())) {
            throw new MethodNotAllowedException("DELETE");
        }
        @Untrusted Optional<String> theme = request.getQuery().get("theme");
        if (theme.isEmpty()) {
            throw new BadRequestException("'theme' parameter is required");
        }
        return Optional.of(getResponse(theme.get().toLowerCase()));
    }

    private Response getResponse(@Untrusted String themeName) {
        if (!themeFilePattern.matcher(themeName).matches()) {
            throw new BadRequestException("'theme' parameter was invalid");
        }

        try {
            Optional<File> found = files.attemptToFind(files.getThemeDirectory(), themeName + ".json");
            if (found.isPresent()) {
                Files.deleteIfExists(found.get().toPath());
            }
            return responseFactory.successResponse();
        } catch (IOException e) {
            return responseFactory.internalErrorResponse(e, e.getMessage());
        }
    }
}
