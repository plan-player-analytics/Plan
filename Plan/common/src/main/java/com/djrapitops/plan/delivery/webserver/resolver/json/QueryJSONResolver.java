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

import com.djrapitops.plan.delivery.domain.DateMap;
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
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.filter.Filter;
import com.djrapitops.plan.storage.database.queries.filter.FilterQuery;
import com.djrapitops.plan.storage.database.queries.filter.QueryFilters;
import com.djrapitops.plan.storage.database.queries.objects.GeoInfoQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.playertable.QueryTablePlayersQuery;
import com.djrapitops.plan.storage.json.JSONStorage;
import com.djrapitops.plan.utilities.java.Maps;
import com.djrapitops.plugin.api.TimeAmount;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Singleton
public class QueryJSONResolver implements Resolver {

    private final QueryFilters filters;

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final JSONStorage jsonStorage;
    private final GraphJSONCreator graphJSONCreator;
    private final Locale locale;
    private final Formatters formatters;

    @Inject
    public QueryJSONResolver(
            QueryFilters filters,
            PlanConfig config,
            DBSystem dbSystem,
            JSONStorage jsonStorage,
            GraphJSONCreator graphJSONCreator,
            Locale locale,
            Formatters formatters
    ) {
        this.filters = filters;
        this.config = config;
        this.dbSystem = dbSystem;
        this.jsonStorage = jsonStorage;
        this.graphJSONCreator = graphJSONCreator;
        this.locale = locale;
        this.formatters = formatters;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        return user.hasPermission("page.players");
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        // Attempt to find previously created result
        try {
            Optional<JSONStorage.StoredJSON> previousResults = request.getQuery().get("timestamp")
                    .flatMap(queryTimestamp -> jsonStorage.fetchExactJson("query", Long.parseLong(queryTimestamp)));
            if (previousResults.isPresent()) {
                return Response.builder()
                        .setMimeType(MimeType.JSON)
                        .setJSONContent(previousResults.get().json)
                        .build();
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Could not parse 'timestamp' into a number. Remove parameter or fix it.");
        }

        String q = request.getQuery().get("q").orElseThrow(() -> new BadRequestException("'q' parameter not set (expecting json array)"));
        String view = request.getQuery().get("view").orElseThrow(() -> new BadRequestException("'view' parameter not set (expecting json object {afterDate, afterTime, beforeDate, beforeTime})"));

        long timestamp = System.currentTimeMillis();

        try {
            q = URLDecoder.decode(q, "UTF-8");
            List<FilterQuery> queries = FilterQuery.parse(q);
            Filter.Result result = filters.apply(queries);

            Map<String, Object> json = Maps.builder(String.class, Object.class)
                    .put("path", result.getResultPath())
                    .put("view", new Gson().fromJson(view, FiltersJSONResolver.ViewJSON.class))
                    .put("timestamp", timestamp)
                    .build();
            if (!result.isEmpty()) {
                json.put("data", getDataFor(result.getResultUUIDs(), view));
            }

            JSONStorage.StoredJSON stored = jsonStorage.storeJson("query", json, timestamp);

            return Response.builder()
                    .setMimeType(MimeType.JSON)
                    .setJSONContent(stored.json)
                    .build();
        } catch (ParseException e) {
            throw new BadRequestException("'view' date format was incorrect (expecting afterDate dd/mm/yyyy, afterTime hh:mm, beforeDate dd/mm/yyyy, beforeTime hh:mm}): " + e.getMessage());
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse json: '" + q + "'" + e.getMessage());
        }
    }

    private Map<String, Object> getDataFor(Set<UUID> playerUUIDs, String view) throws ParseException {
        FiltersJSONResolver.ViewJSON viewJSON = new Gson().fromJson(view, FiltersJSONResolver.ViewJSON.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm");
        long after = dateFormat.parse(viewJSON.afterDate + " " + viewJSON.afterTime).getTime();
        long before = dateFormat.parse(viewJSON.beforeDate + " " + viewJSON.beforeTime).getTime();

        return Maps.builder(String.class, Object.class)
                .put("players", getPlayersTableData(playerUUIDs, after, before))
                .put("activity", getActivityGraphData(playerUUIDs, after, before))
                .put("geolocation", getGeolocationData(playerUUIDs))
                .put("sessions", getSessionSummaryData(playerUUIDs, after, before))
                .build();
    }

    private Map<String, String> getSessionSummaryData(Set<UUID> playerUUIDs, long after, long before) {
        Database database = dbSystem.getDatabase();
        Map<String, Long> summary = database.query(SessionQueries.summaryOfPlayers(playerUUIDs, after, before));
        Map<String, String> formattedSummary = new HashMap<>();
        Formatter<Long> timeAmount = formatters.timeAmount();
        for (Map.Entry<String, Long> entry : summary.entrySet()) {
            formattedSummary.put(entry.getKey(), timeAmount.apply(entry.getValue()));
        }
        formattedSummary.put("total_sessions", Long.toString(summary.get("total_sessions")));
        formattedSummary.put("average_sessions", Long.toString(summary.get("average_sessions")));
        return formattedSummary;
    }

    private Map<String, Object> getGeolocationData(Set<UUID> playerUUIDs) {
        Database database = dbSystem.getDatabase();
        return graphJSONCreator.createGeolocationJSON(
                database.query(GeoInfoQueries.networkGeolocationCounts(playerUUIDs))
        );
    }

    private Map<String, Object> getActivityGraphData(Set<UUID> playerUUIDs, long after, long before) {
        Database database = dbSystem.getDatabase();
        Long threshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        long twoMonthsBeforeLastDate = before - TimeAmount.MONTH.toMillis(2L);
        long stopDate = Math.max(twoMonthsBeforeLastDate, after);

        DateMap<Map<String, Integer>> activityData = new DateMap<>();
        for (long time = before; time >= stopDate; time -= TimeAmount.WEEK.toMillis(1L)) {
            activityData.put(time, database.query(NetworkActivityIndexQueries.fetchActivityIndexGroupingsOn(time, threshold, playerUUIDs)));
        }

        return graphJSONCreator.createActivityGraphJSON(activityData);
    }

    private Map<String, Object> getPlayersTableData(Set<UUID> playerUUIDs, long after, long before) {
        Database database = dbSystem.getDatabase();
        return new PlayersTableJSONCreator(
                database.query(new QueryTablePlayersQuery(playerUUIDs, after, before, config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD))),
                Collections.emptyMap(),
                config.get(DisplaySettings.OPEN_PLAYER_LINKS_IN_NEW_TAB),
                formatters, locale
        ).toJSONMap();
    }
}
