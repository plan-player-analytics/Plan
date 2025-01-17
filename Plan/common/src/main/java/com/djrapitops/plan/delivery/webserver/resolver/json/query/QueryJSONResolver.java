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

import com.djrapitops.plan.delivery.domain.DateMap;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.InputFilterDto;
import com.djrapitops.plan.delivery.domain.datatransfer.InputQueryDto;
import com.djrapitops.plan.delivery.domain.datatransfer.PlayerListDto;
import com.djrapitops.plan.delivery.domain.datatransfer.ViewDto;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.json.PlayersTableJSONCreator;
import com.djrapitops.plan.delivery.rendering.json.graphs.GraphJSONCreator;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.RequestBodyConverter;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.extension.implementation.storage.queries.playertable.ExtensionQueryResultTableDataQuery;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.filter.Filter;
import com.djrapitops.plan.storage.database.queries.filter.QueryFilters;
import com.djrapitops.plan.storage.database.queries.objects.GeoInfoQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.playertable.QueryTablePlayersQuery;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Maps;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.playeranalytics.plugin.scheduling.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

@Singleton
@Path("/v1/query")
public class QueryJSONResolver implements Resolver {

    private final QueryFilters filters;

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final JSONStorage jsonStorage;
    private final GraphJSONCreator graphJSONCreator;
    private final Locale locale;
    private final Formatters formatters;
    private final Gson gson;

    @Inject
    public QueryJSONResolver(
            QueryFilters filters,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo, JSONStorage jsonStorage,
            GraphJSONCreator graphJSONCreator,
            Locale locale,
            Formatters formatters,
            Gson gson
    ) {
        this.filters = filters;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.jsonStorage = jsonStorage;
        this.graphJSONCreator = graphJSONCreator;
        this.locale = locale;
        this.formatters = formatters;
        this.gson = gson;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        return user.hasPermission(WebPermission.ACCESS_QUERY)
                || user.hasPermission(WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_CALENDAR)
                || user.hasPermission(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_CALENDAR)
                || user.hasPermission(WebPermission.PAGE_NETWORK_GEOLOCATIONS_MAP)
                || user.hasPermission(WebPermission.PAGE_SERVER_GEOLOCATIONS_MAP);
    }

    @GET
    @Operation(
            description = "Perform a query or get cached results. Use q to do new query, timestamp to see cached query.",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON)),
                    @ApiResponse(responseCode = "400 (invalid view)", description = "If 'view' date valueFormats does not match afterDate dd/mm/yyyy, afterTime hh:mm, beforeDate dd/mm/yyyy, beforeTime hh:mm"),
                    @ApiResponse(responseCode = "400 (no query)", description = "If request body is empty and 'q' request parameter is not given"),
                    @ApiResponse(responseCode = "400 (invalid query)", description = "If request body is empty and 'q' json request parameter doesn't contain 'view' property"),
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "timestamp", description = "Epoch millisecond for cached query"),
                    @Parameter(in = ParameterIn.QUERY, name = "q", description = "URI encoded json, alternative is to POST in request body", schema = @Schema(implementation = InputQueryDto.class))
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = InputQueryDto.class)))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(@Untrusted Request request) {
        Optional<WebUser> user = request.getUser();
        boolean canAccessCache = user.map(u -> u.hasPermission(WebPermission.ACCESS_QUERY)).orElse(true);
        Optional<Response> cachedResult = canAccessCache ? checkForCachedResult(request) : Optional.empty();
        if (cachedResult.isPresent()) return cachedResult.get();

        InputQueryDto inputQuery = parseInputQuery(request);
        @Untrusted List<InputFilterDto> queries = inputQuery.getFilters();

        // Check user has permission for the filter if login is enabled.
        if (user.isPresent()) {
            Optional<Response> errorResponse = checkFilterPermissions(queries, user.get());
            if (errorResponse.isPresent()) {
                return errorResponse.get();
            }
        }

        Filter.Result result = filters.apply(queries);
        List<Filter.ResultPath> resultPath = result.getInverseResultPath();
        Collections.reverse(resultPath);

        return buildAndStoreResponse(inputQuery, result, resultPath);
    }

    private Optional<Response> checkFilterPermissions(List<InputFilterDto> queries, WebUser user) {
        for (InputFilterDto filter : queries) {
            @Untrusted String filterKind = filter.getKind();
            if (!isFilterAllowed(user, filterKind)) {
                return Optional.of(Response.builder()
                        .setStatus(403)
                        .setJSONContent("{\"error\": \"You don't have permission to use one of the given filters\"}")
                        .build());
            }
        }
        return Optional.empty();
    }

    private boolean isFilterAllowed(WebUser user, @Untrusted String filterKind) {
        for (WebPermission allowed : getAllowingPermissions(filterKind)) {
            if (user.hasPermission(allowed)) {
                return true;
            }
        }
        return false;
    }

    private WebPermission[] getAllowingPermissions(@Untrusted String filterKind) {
        switch (filterKind) {
            case "playedBetween":
                return new WebPermission[]{
                        WebPermission.ACCESS_QUERY,
                        WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_CALENDAR,
                        WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_CALENDAR
                };
            case "geolocations":
                return new WebPermission[]{
                        WebPermission.ACCESS_QUERY,
                        WebPermission.PAGE_NETWORK_GEOLOCATIONS_MAP,
                        WebPermission.PAGE_SERVER_GEOLOCATIONS_MAP
                };
            default:
                return new WebPermission[]{WebPermission.ACCESS_QUERY};
        }
    }

    private InputQueryDto parseInputQuery(@Untrusted Request request) {
        if (request.getRequestBody().length == 0) {
            return parseInputQueryFromQueryParams(request);
        } else {
            return RequestBodyConverter.bodyJson(request, gson, InputQueryDto.class);
        }
    }

    private InputQueryDto parseInputQueryFromQueryParams(@Untrusted Request request) {
        @Untrusted String q = request.getQuery().get("q").orElseThrow(() -> new BadRequestException("'q' parameter not set (expecting json array)"));
        try {
            @Untrusted String query = URLDecoder.decode(q, StandardCharsets.UTF_8);
            @Untrusted List<InputFilterDto> queryFilters = InputFilterDto.parse(query, gson);
            ViewDto view = request.getQuery().get("view")
                    .map(viewJson -> gson.fromJson(viewJson, ViewDto.class))
                    .orElseThrow(() -> new BadRequestException("'view' parameter not set (expecting json object {afterDate, afterTime, beforeDate, beforeTime})"));
            return new InputQueryDto(view, queryFilters);
        } catch (IOException e) {
            throw new BadRequestException("Failed to decode json");
        }
    }

    private Optional<Response> checkForCachedResult(@Untrusted Request request) {
        try {
            return request.getQuery().get("timestamp")
                    .map(Long::parseLong)
                    .flatMap(queryTimestamp -> jsonStorage.fetchExactJson("query", queryTimestamp))
                    .map(results -> Response.builder()
                            .setMimeType(MimeType.JSON)
                            .setJSONContent(results.json)
                            .build());
        } catch (@Untrusted NumberFormatException e) {
            throw new BadRequestException("Could not parse 'timestamp' into a number. Remove parameter or fix it.");
        }
    }

    private Response buildAndStoreResponse(InputQueryDto input, Filter.Result result, List<Filter.ResultPath> resultPath) {
        try {
            long timestamp = System.currentTimeMillis();
            @Untrusted Map<String, Object> json = Maps.builder(String.class, Object.class)
                    .put("path", resultPath)
                    .put("view", input.getView())
                    .put("filters", input.getFilters()) // filters json may contain untrusted data
                    .put("timestamp", timestamp)
                    .build();
            if (!result.isEmpty()) {
                json.put("data", getDataFor(result.getResultUserIds(), input.getView()));
            }

            JSONStorage.StoredJSON stored = jsonStorage.storeJson("query", json, timestamp);

            return Response.builder()
                    .setMimeType(MimeType.JSON)
                    .setJSONContent(stored.json)
                    .build();
        } catch (ParseException e) {
            throw new BadRequestException("'view' date format was incorrect (expecting afterDate dd/mm/yyyy, afterTime hh:mm, beforeDate dd/mm/yyyy, beforeTime hh:mm})");
        }
    }

    private Map<String, Object> getDataFor(Set<Integer> userIds, ViewDto view) throws ParseException {
        long after = view.getAfterEpochMs();
        long before = view.getBeforeEpochMs();
        List<ServerUUID> serverUUIDs = view.getServerUUIDs();

        return Maps.builder(String.class, Object.class)
                .put("players", getPlayersTableData(userIds, serverUUIDs, after, before))
                .put("activity", getActivityGraphData(userIds, serverUUIDs, after, before))
                .put("geolocation", getGeolocationData(userIds))
                .put("sessions", getSessionSummaryData(userIds, serverUUIDs, after, before))
                .build();
    }

    private Map<String, String> getSessionSummaryData(Set<Integer> userIds, List<ServerUUID> serverUUIDs, long after, long before) {
        Database database = dbSystem.getDatabase();
        Map<String, Long> summary = database.query(SessionQueries.summaryOfPlayers(userIds, serverUUIDs, after, before));
        Map<String, String> formattedSummary = new HashMap<>();
        Formatter<Long> timeAmount = formatters.timeAmount();
        for (Map.Entry<String, Long> entry : summary.entrySet()) {
            formattedSummary.put(entry.getKey(), timeAmount.apply(entry.getValue()));
        }
        formattedSummary.put("total_sessions", Long.toString(summary.get("total_sessions")));
        formattedSummary.put("average_sessions", Long.toString(summary.get("average_sessions")));
        return formattedSummary;
    }

    private Map<String, Object> getGeolocationData(Set<Integer> userIds) {
        Database database = dbSystem.getDatabase();
        return graphJSONCreator.createGeolocationJSON(
                database.query(GeoInfoQueries.networkGeolocationCounts(userIds))
        );
    }

    private Map<String, Object> getActivityGraphData(Set<Integer> userIds, List<ServerUUID> serverUUIDs, long after, long before) {
        Database database = dbSystem.getDatabase();
        Long threshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        long twoMonthsBeforeLastDate = before - TimeAmount.MONTH.toMillis(2L);
        long stopDate = Math.max(twoMonthsBeforeLastDate, after);

        DateMap<Map<String, Integer>> activityData = new DateMap<>();
        for (long time = before; time >= stopDate; time -= TimeAmount.WEEK.toMillis(1L)) {
            activityData.put(time, database.query(NetworkActivityIndexQueries.fetchActivityIndexGroupingsOn(time, threshold, userIds, serverUUIDs)));
        }

        return graphJSONCreator.createActivityGraphJSON(activityData);
    }

    private PlayerListDto getPlayersTableData(Set<Integer> userIds, List<ServerUUID> serverUUIDs, long after, long before) {
        Database database = dbSystem.getDatabase();
        return new PlayersTableJSONCreator(
                database.query(new QueryTablePlayersQuery(userIds, serverUUIDs, after, before, config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD))),
                database.query(new ExtensionQueryResultTableDataQuery(serverInfo.getServerUUID(), userIds)),
                config.get(DisplaySettings.OPEN_PLAYER_LINKS_IN_NEW_TAB),
                formatters, locale
        ).toPlayerList();
    }
}
