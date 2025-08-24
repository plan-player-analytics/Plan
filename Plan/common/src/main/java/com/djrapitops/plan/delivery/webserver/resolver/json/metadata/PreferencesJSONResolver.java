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
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Represents /v1/preferences endpoint.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/preferences")
public class PreferencesJSONResolver implements NoAuthResolver {

    private final PlanConfig config;
    private final DBSystem dbSystem;

    @Inject
    public PreferencesJSONResolver(PlanConfig config, DBSystem dbSystem) {
        this.config = config;
        this.dbSystem = dbSystem;
    }

    @GET
    @Operation(
            description = "Get user preferences (if they exist) and default preferences",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON))
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(@Untrusted Request request) {
        Preferences defaultPreferences = config.getDefaultPreferences();

        return Optional.of(Response.builder()
                .setJSONContent(Maps.builder(String.class, Preferences.class)
                        .put("defaultPreferences", defaultPreferences)
                        .put("preferences", getUserPreferences(request))
                        .build()
                ).build());
    }

    private Preferences getUserPreferences(Request request) {
        // No user               -> no preferences
        // No stored preferences -> no preferences
        return request.getUser()
                .flatMap(user -> dbSystem.getDatabase().query(WebUserQueries.fetchPreferences(user)))
                .orElse(null);
    }
}
