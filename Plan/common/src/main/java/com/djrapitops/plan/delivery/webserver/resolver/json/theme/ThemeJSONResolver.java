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

import com.djrapitops.plan.delivery.domain.datatransfer.ThemeDto;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.utilities.dev.Untrusted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents /v1/theme endpoint.
 * <p>
 * Used to read theme contents, always allowed.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/theme")
public class ThemeJSONResolver implements NoAuthResolver {

    private static final Pattern themeFilePattern = Pattern.compile("[a-zA-Z0-9-]*");

    private final ResponseFactory responseFactory;

    @Inject
    public ThemeJSONResolver(ResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @GET
    @Operation(
            description = "Get theme json for a name",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON, schema = @Schema(implementation = ThemeDto.class))),
                    @ApiResponse(responseCode = "400", description = "If 'theme' parameter is not specified or invalid")
            },
            parameters = {@Parameter(in = ParameterIn.QUERY, name = "theme", description = "Name of the theme, alphanumeric with dashes", examples = {
                    @ExampleObject("default"),
                    @ExampleObject("color-blind")
            }), @Parameter(in = ParameterIn.QUERY, name = "onlyJar", description = "Only accept themes from the jar", examples = {@ExampleObject("true")})},
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        Optional<String> theme = request.getQuery().get("theme");
        if (theme.isEmpty()) {
            throw new BadRequestException("'theme' parameter is required");
        }
        return Optional.of(getThemeResponse(theme.get(), request));
    }

    private Response getThemeResponse(@Untrusted String themeName, Request request) {
        if (themeName.isEmpty()) {
            throw new BadRequestException("'theme' name can not be empty");
        }
        if (themeFilePattern.matcher(themeName).matches() || StringUtils.containsAny(themeName, '\n', '\t')) {
            return responseFactory.themeResponse(themeName, request);
        } else {
            throw new BadRequestException("'theme' parameter was invalid");
        }
    }
}
