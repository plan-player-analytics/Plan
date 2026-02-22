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
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.MethodNotAllowedException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.storage.file.PlanFiles;
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
import org.apache.commons.lang3.Strings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    private final PlanFiles files;
    private final ResponseFactory responseFactory;
    private final Gson gson;

    @Inject
    public SaveThemeJSONResolver(PlanFiles files, ResponseFactory responseFactory, Gson gson) {
        this.files = files;
        this.responseFactory = responseFactory;
        this.gson = gson;
    }

    private static boolean isInvalid(ThemeDto result) {
        return result == null
                || result.getName() == null || result.getName().isEmpty()
                || !themeFilePattern.matcher(result.getName()).matches()
                || result.getColors() == null || result.getColors().isEmpty()
                || result.getNightColors() == null || result.getNightColors().isEmpty()
                || result.getUseCases() == null || result.getUseCases().isEmpty()
                || result.getNightModeUseCases() == null || result.getNightModeUseCases().isEmpty();
    }

    private static void validateVariable(List<String> issues, String value, String prefixedKey) {
        if (!Strings.CS.startsWith(value, "var(--color-")) {
            issues.add(prefixedKey + " is not a color variable");
        }
        if (!Strings.CS.endsWith(value, ")")) {
            issues.add(prefixedKey + " is not a color variable");
        }
        if (!colorPattern.matcher(value).matches()) {
            issues.add(prefixedKey + " has invalid character");
        }
    }

    private static void validateListVariables(List<String> issues, List<String> value, String prefixedKey) {
        for (String color : value) {
            if (!themeFilePattern.matcher(color).matches()) {
                issues.add(prefixedKey + " has invalid character");
            }
        }
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
        if (!"POST".equals(request.getMethod())) {
            throw new MethodNotAllowedException("POST");
        }
        @Untrusted Optional<String> theme = request.getQuery().get("theme");
        if (theme.isEmpty()) {
            throw new BadRequestException("'theme' parameter is required");
        }
        return Optional.of(getResponse(theme.get().toLowerCase(), request));
    }

    private Response getResponse(@Untrusted String themeName, Request request) {
        if (!themeFilePattern.matcher(themeName).matches() || StringUtils.containsAny(themeName, '\n', '\t')) {
            throw new BadRequestException("'theme' parameter was invalid");
        }
        if (themeName.isEmpty()) {
            throw new BadRequestException("'theme' name can not be empty");
        }
        if (themeName.length() > 100) {
            throw new BadRequestException("'theme' name was too long");
        }

        @Untrusted byte[] requestBody = request.getRequestBody();
        if (requestBody == null) throw new BadRequestException("Request body is required");
        try {
            @Untrusted ThemeDto result = gson.fromJson(new String(requestBody, StandardCharsets.UTF_8), ThemeDto.class);
            if (isInvalid(result)) {
                throw new BadRequestException("Body needs to be a valid theme file");
            }
            if (!result.getName().equals(themeName)) {
                throw new BadRequestException("name in the body must match 'theme' parameter");
            }

            List<String> issues = new ArrayList<>();

            validateUseCases("", result.getUseCases(), issues);
            validateUseCases("", result.getNightModeUseCases(), issues);

            if (!issues.isEmpty()) {
                throw new BadRequestException("Invalid request body: " + issues.toString());
            }

            java.nio.file.Path themeDirectory = files.getThemeDirectory();
            java.nio.file.Path themeFile = themeDirectory.resolve(themeName + ".json");
            if (!themeFile.startsWith(themeDirectory)) {
                throw new BadRequestException("'theme' parameter was invalid");
            }

            Files.write(themeFile, gson.toJson(result).getBytes(StandardCharsets.UTF_8), PlanFiles.replaceIfExists());

            request.getQuery().get("originalName")
                    .ifPresent(originalName -> deleteOriginal(themeName, originalName, themeDirectory));
            return responseFactory.successResponse();
        } catch (JsonSyntaxException e) {
            throw new BadRequestException("Request body was invalid json");
        } catch (IOException e) {
            return responseFactory.internalErrorResponse(e, e.getMessage());
        }
    }

    void deleteOriginal(@Untrusted String themeName, @Untrusted String originalName, java.nio.file.Path themeDirectory) {
        if (originalName.equals(themeName)) return; // Theme was not renamed.

        // All these checks are against trying to delete other themes than the one they're editing.
        if (!themeFilePattern.matcher(originalName).matches()) {
            throw new BadRequestException("'originalTheme' parameter was invalid, theme could have been renamed by another user");
        }
        java.nio.file.Path originalThemeFile = themeDirectory.resolve(originalName + ".json");
        if (!originalThemeFile.startsWith(themeDirectory)) {
            throw new BadRequestException("'originalTheme' parameter was invalid, theme could have been renamed by another user");
        }
        if (!Files.exists(originalThemeFile)) {
            throw new BadRequestException("'originalTheme' parameter was invalid, theme could have been renamed by another user");
        }
        try {
            ThemeDto original = gson.fromJson(Files.readString(originalThemeFile), ThemeDto.class);
            if (!original.getName().equals(originalName)) {
                throw new BadRequestException("'originalTheme' parameter was invalid, doesn't match original file");
            }

            Files.delete(originalThemeFile);
        } catch (JsonSyntaxException | IOException e) {
            throw new BadRequestException("'originalTheme' parameter was invalid, could not parse or delete original file. Delete manually.");
        }
    }

    private void validateUseCases(String prefix, Map<String, Object> useCases, List<String> issues) {
        for (Map.Entry<String, Object> entry : useCases.entrySet()) {
            String prefixedKey = prefix + entry.getKey();
            Object value = entry.getValue();
            try {
                if (value instanceof String) {
                    validateVariable(issues, (String) value, prefixedKey);
                } else if (value instanceof List) {
                    validateListVariables(issues, (List<String>) value, prefixedKey);
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
