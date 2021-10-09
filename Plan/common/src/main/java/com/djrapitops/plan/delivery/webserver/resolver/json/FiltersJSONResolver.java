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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.datatransfer.ServerDto;
import com.djrapitops.plan.delivery.formatting.Formatter;
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
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
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
        return user.hasPermission("page.players");
    }

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

    private List<Double[]> fetchViewGraphPoints() {
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
    static class FilterDto implements Comparable<FilterDto> {
        final String kind;
        final Map<String, Object> options;
        final String[] expectedParameters;

        public FilterDto(String kind, Filter filter) {
            this.kind = kind;
            this.options = filter.getOptions();
            this.expectedParameters = filter.getExpectedParameters();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FilterDto that = (FilterDto) o;
            return Objects.equals(kind, that.kind) && Objects.equals(options, that.options) && Arrays.equals(expectedParameters, that.expectedParameters);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(kind, options);
            result = 31 * result + Arrays.hashCode(expectedParameters);
            return result;
        }

        @Override
        public int compareTo(FilterDto o) {
            return String.CASE_INSENSITIVE_ORDER.compare(this.kind, o.kind);
        }
    }

    /**
     * JSON serialization class.
     */
    static class ViewDto {
        final String afterDate;
        final String afterTime;
        final String beforeDate;
        final String beforeTime;
        final List<ServerDto> servers;

        public ViewDto(Formatters formatters, List<ServerDto> servers) {
            this.servers = servers;
            long now = System.currentTimeMillis();
            long monthAgo = now - TimeUnit.DAYS.toMillis(30);

            Formatter<Long> formatter = formatters.javascriptDateFormatterLong();
            String[] after = StringUtils.split(formatter.apply(monthAgo), " ");
            String[] before = StringUtils.split(formatter.apply(now), " ");

            this.afterDate = after[0];
            this.afterTime = after[1];
            this.beforeDate = before[0];
            this.beforeTime = before[1];
        }
    }

    /**
     * JSON serialization class.
     */
    class FilterResponseDto {
        final List<FilterDto> filters;
        final ViewDto view;
        final List<Double[]> viewPoints;

        public FilterResponseDto(Map<String, Filter> filtersByKind, ViewDto view, List<Double[]> viewPoints) {
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
