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
package com.djrapitops.plan.delivery.webserver.resolver.json.query;

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.FilterDto;
import com.djrapitops.plan.delivery.domain.datatransfer.ViewDto;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.json.JSONFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.Graphs;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.LineGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.filter.Filter;
import com.djrapitops.plan.storage.database.queries.filter.QueryFilters;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@Path("/v1/filters")
public class FiltersJSONResolver implements Resolver {

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final QueryFilters filters;
    private final JSONFactory jsonFactory;
    private final Graphs graphs;
    private final Formatters formatters;
    private final ErrorLogger errorLogger;

    @Inject
    public FiltersJSONResolver(
            ServerInfo serverInfo,
            DBSystem dbSystem,
            QueryFilters filters,
            JSONFactory jsonFactory,
            Graphs graphs,
            Formatters formatters,
            ErrorLogger errorLogger
    ) {
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.filters = filters;
        this.jsonFactory = jsonFactory;
        this.graphs = graphs;
        this.formatters = formatters;
        this.errorLogger = errorLogger;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        return user.hasPermission(WebPermission.ACCESS_QUERY);
    }

    @GET
    @Operation(
            description = "Get list of available filters, view and graph points for visualizing the view",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON, schema = @Schema(implementation = FilterResponseDto.class)))
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
                .setJSONContent(new FilterResponseDto(
                        filters.getFilters(),
                        new ViewDto(formatters, jsonFactory.listServers().get("servers")),
                        fetchViewGraphPoints()
                )).build();
    }

    private List<Number[]> fetchViewGraphPoints() {
        List<DateObj<Integer>> data = dbSystem.getDatabase().query(TPSQueries.fetchViewPreviewGraphData(serverInfo.getServerUUID()));
        Long earliestStart = dbSystem.getDatabase().query(SessionQueries.earliestSessionStart());
        data.add(0, new DateObj<>(earliestStart, 1));

        LineGraph.GapStrategy gapStrategy = new LineGraph.GapStrategy(
                true,
                TimeUnit.MINUTES.toMillis(16), // Acceptable gap
                TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(30),
                0.0
        );
        return graphs.line().lineGraph(Lists.map(data, Point::fromDateObj), gapStrategy).getPoints()
                .stream().map(Point::toArray).collect(Collectors.toList());
    }

    /**
     * JSON serialization class.
     */
    class FilterResponseDto {
        final List<FilterDto> filters;
        final ViewDto view;
        final List<Number[]> viewPoints;

        public FilterResponseDto(Map<String, Filter> filtersByKind, ViewDto view, List<Number[]> viewPoints) {
            this.viewPoints = viewPoints;
            this.filters = new ArrayList<>();
            for (Map.Entry<String, Filter> entry : filtersByKind.entrySet()) {
                try {
                    filters.add(new FilterDto(entry.getKey(), entry.getValue()));
                } catch (Exception e) {
                    errorLogger.error(e, ErrorContext.builder()
                            .whatToDo("Report this, filter '" + entry.getKey() + "' has implementation error.")
                            .related(entry.getValue())
                            .build());
                }
            }
            Collections.sort(filters);
            this.view = view;
        }
    }
}
