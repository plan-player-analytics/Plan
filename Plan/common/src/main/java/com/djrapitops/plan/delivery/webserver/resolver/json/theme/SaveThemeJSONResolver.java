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
import com.djrapitops.plan.delivery.domain.datatransfer.ThemeDto;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Endpoint to store themes from theme editor.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/saveTheme")
public class SaveThemeJSONResolver implements Resolver {

    private static final Pattern themeFilePattern = Pattern.compile("[a-zA-Z0-9-]*");
    private static final Pattern colorPattern = Pattern.compile("[a-zA-Z0-9-)(]*");

    private final ResponseFactory responseFactory;
    private final Gson gson;

    @Inject
    public SaveThemeJSONResolver(ResponseFactory responseFactory, Gson gson) {
        this.responseFactory = responseFactory;
        this.gson = gson;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        return user.hasPermission(WebPermission.MANAGE_THEMES);
    }

    @POST
    @Operation(
            description = "Save theme json with a name",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON)),
                    @ApiResponse(responseCode = "400", description = "If 'theme' parameter is not specified or invalid"),
                    @ApiResponse(responseCode = "400", description = "If request body is invalid")
            },
            parameters = @Parameter(in = ParameterIn.QUERY, name = "theme", description = "Name of the theme, alphanumeric with dashes", examples = {
                    @ExampleObject("default"),
                    @ExampleObject("color-blind")
            }),
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ThemeDto.class)))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        @Untrusted Optional<String> theme = request.getQuery().get("theme");
        if (theme.isEmpty()) {
            return Optional.of(responseFactory.badRequest("'theme' parameter is required", "/v1/saveTheme"));
        }
        return Optional.of(getResponse(theme.get().toLowerCase(), request));
    }

    private Response getResponse(@Untrusted String themeName, Request request) {
        if (!themeFilePattern.matcher(themeName).matches()) {
            return responseFactory.badRequest("'theme' parameter was invalid", "/v1/target");
        }

        @Untrusted byte[] requestBody = request.getRequestBody();
        try {
            @Untrusted ThemeDto result = gson.fromJson(new String(requestBody, StandardCharsets.UTF_8), ThemeDto.class);

            List<String> issues = new ArrayList<>();

            validateUseCases("", result.getUseCases(), issues);
            validateUseCases("", result.getNightModeUseCases(), issues);

            if (!issues.isEmpty()) {
                return responseFactory.badRequest("Invalid request body: " + issues.toString(), "/v1/saveTheme");
            }

            // TODO save the actual theme file

            return responseFactory.successResponse();
        } catch (JsonSyntaxException e) {
            return responseFactory.badRequest("Request body was invalid json", "/v1/saveTheme");
        }
    }

    private void validateUseCases(String prefix, Map<String, Object> useCases, List<String> issues) {
        for (Map.Entry<String, Object> entry : useCases.entrySet()) {
            String prefixedKey = prefix + entry.getKey();
            Object value = entry.getValue();
            try {
                if (value instanceof String) {
                    if (!StringUtils.startsWith((String) value, "var(--color-")) {
                        issues.add(prefixedKey + " is not a color variable");
                    }
                    if (!StringUtils.endsWith((String) value, ")")) {
                        issues.add(prefixedKey + " is not a color variable");
                    }
                    if (!colorPattern.matcher((String) value).matches()) {
                        issues.add(prefixedKey + " has invalid character");
                    }
                } else if (value instanceof List) {
                    for (String color : (List<String>) value) {
                        if (!themeFilePattern.matcher(color).matches()) {
                            issues.add(prefixedKey + " has invalid character");
                        }
                    }
                } else if (value instanceof Map) {
                    validateUseCases(prefixedKey + '.', (Map<String, Object>) value, issues);
                } else {
                    issues.add(prefixedKey + " had unknown type (Can be object, string or string[])");
                }
            } catch (ClassCastException e) {
                issues.add(prefixedKey + " had invalid type (Can be object, string or string[])");
            }
        }

    }
}
