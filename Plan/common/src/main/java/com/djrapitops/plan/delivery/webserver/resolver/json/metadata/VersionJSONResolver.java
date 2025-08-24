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

import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.version.VersionChecker;
import com.djrapitops.plan.version.VersionInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves requests for /v1/version.
 *
 * @author Kopo942
 */
@Path("/v1/version")
public class VersionJSONResolver implements NoAuthResolver {

    private final VersionChecker versionChecker;
    private final String currentVersion;

    @Inject
    public VersionJSONResolver(
            @Named("currentVersion") String currentVersion,
            VersionChecker versionChecker
    ) {
        this.currentVersion = currentVersion;
        this.versionChecker = versionChecker;
    }

    @GET
    @Operation(
            description = "Get Plan version and update information",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON)),
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse());
    }

    private Response getResponse() {
        Map<String, Object> json = new HashMap<>();
        Optional<VersionInfo> newVersion = versionChecker.getNewVersionAvailable();
        boolean updateAvailable = newVersion.isPresent();

        json.put("currentVersion", this.currentVersion);
        json.put("updateAvailable", updateAvailable);

        if (updateAvailable) {
            json.put("newVersion", newVersion.get().getVersion().asString());
            json.put("downloadUrl", newVersion.get().getDownloadUrl());
            json.put("changelogUrl", newVersion.get().getChangeLogUrl());
            json.put("isRelease", newVersion.get().isRelease());
        }

        return Response.builder().setJSONContent(json).build();
    }
}
