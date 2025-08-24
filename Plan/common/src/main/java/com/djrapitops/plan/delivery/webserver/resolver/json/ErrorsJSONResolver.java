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
package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.storage.file.PlanFiles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Path("/v1/errors")
public class ErrorsJSONResolver implements Resolver {

    private final PlanFiles files;

    @Inject
    public ErrorsJSONResolver(PlanFiles files) {
        this.files = files;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().orElse(new WebUser("")).hasPermission(WebPermission.ACCESS_ERRORS);
    }

    @GET
    @Operation(
            description = "Get list of Plan error logs",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of error files and their contents", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ErrorFile.class))))
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse());
    }

    private Response getResponse() {
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(loadErrorLogs())
                .build();
    }

    private List<ErrorFile> loadErrorLogs() {
        File[] logFiles = this.files.getLogsFolder().listFiles();
        // Can't use Collections.emptyList since Gson doesn't serialize it
        if (logFiles == null || logFiles.length == 0) return new ArrayList<>();

        List<ErrorFile> errorFiles = new ArrayList<>();
        for (File file : logFiles) {
            errorFiles.add(new ErrorFile(file.getName(), read(file)));
        }

        return errorFiles;
    }

    private List<String> read(File file) {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return lines.collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.singletonList("Failed to read " + file.getAbsolutePath() + ": " + e);
        }
    }

    private static class ErrorFile {
        private final String fileName;
        private final List<String> contents;

        public ErrorFile(String fileName, List<String> contents) {
            this.fileName = fileName;
            this.contents = contents;
        }

        public String getFileName() {
            return fileName;
        }

        public List<String> getContents() {
            return contents;
        }
    }
}
