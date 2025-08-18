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
import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Creates JSON payload for /server-page Performance tab.
 *
 * @author AuroraLS3
 */
@Singleton
@Path("/v1/network/performanceOverview")
public class NetworkPerformanceJSONResolver implements Resolver {

    private final PlanConfig config;
    private final DBSystem dbSystem;

    private final Formatter<Double> decimals;
    private final Formatter<Long> timeAmount;
    private final Formatter<Double> percentage;
    private final Formatter<Double> byteSize;
    private final Gson gson;

    @Inject
    public NetworkPerformanceJSONResolver(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters,
            Gson gson
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        decimals = formatters.decimals();
        percentage = formatters.percentage();
        timeAmount = formatters.timeAmount();
        byteSize = formatters.byteSize();
        this.gson = gson;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().orElse(new WebUser("")).hasPermission(WebPermission.PAGE_NETWORK_PERFORMANCE);
    }

    @GET
    @Operation(
            description = "Get performance overview information for multiple servers",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = MimeType.JSON, examples = {
                            @ExampleObject("{\"numbers\": {}}")
                    }))
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "servers", required = true, description = "JSON list of server uuids (URI encoded)", example = "%5B%22a779e107-0474-4d9f-8f4d-f1efb068d32e%22%5D (is [\"a779e107-0474-4d9f-8f4d-f1efb068d32e\"])")
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        List<ServerUUID> serverUUIDs = request.getQuery().get("servers")
                .map(this::getUUIDList)
                .orElse(Collections.emptyList())
                .stream().map(ServerUUID::from)
                .collect(Collectors.toList());
        return Optional.of(Response.builder()
                .setJSONContent(createJSONAsMap(serverUUIDs))
                .build());
    }

    private List<UUID> getUUIDList(@Untrusted String jsonString) {
        return gson.fromJson(jsonString, new TypeToken<List<UUID>>() {}.getType());
    }

    public Map<String, Object> createJSONAsMap(Collection<ServerUUID> serverUUIDs) {
        Map<String, Object> serverOverview = new HashMap<>();
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        Map<Integer, List<TPS>> tpsData = db.query(TPSQueries.fetchTPSDataOfServers(monthAgo, now, serverUUIDs));

        serverOverview.put("numbers", createNumbersMap(tpsData));
        return serverOverview;
    }

    private Map<String, Object> createNumbersMap(Map<Integer, List<TPS>> tpsData) {
        long now = System.currentTimeMillis();
        long dayAgo = now - TimeUnit.DAYS.toMillis(1L);
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);

        Map<String, Object> numbers = new HashMap<>();

        List<TPS> tpsDataOfAllServers = new ArrayList<>();
        tpsData.values().forEach(tpsDataOfAllServers::addAll);
        TPSMutator tpsDataMonth = new TPSMutator(tpsDataOfAllServers);
        TPSMutator tpsDataWeek = tpsDataMonth.filterDataBetween(weekAgo, now);
        TPSMutator tpsDataDay = tpsDataWeek.filterDataBetween(dayAgo, now);

        Map<Integer, TPSMutator> mutatorsOfServersMonth = new HashMap<>();
        Map<Integer, TPSMutator> mutatorsOfServersWeek = new HashMap<>();
        Map<Integer, TPSMutator> mutatorsOfServersDay = new HashMap<>();
        for (Map.Entry<Integer, List<TPS>> entry : tpsData.entrySet()) {
            TPSMutator mutator = new TPSMutator(entry.getValue());
            mutatorsOfServersMonth.put(entry.getKey(), mutator);
            mutatorsOfServersWeek.put(entry.getKey(), mutator.filterDataBetween(weekAgo, now));
            mutatorsOfServersDay.put(entry.getKey(), mutator.filterDataBetween(dayAgo, now));
        }

        Double tpsThreshold = config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED);
        numbers.put("low_tps_spikes_30d", tpsDataMonth.lowTpsSpikeCount(tpsThreshold));
        numbers.put("low_tps_spikes_7d", tpsDataWeek.lowTpsSpikeCount(tpsThreshold));
        numbers.put("low_tps_spikes_24h", tpsDataDay.lowTpsSpikeCount(tpsThreshold));

        long downtimeMonth = getTotalDowntime(mutatorsOfServersMonth);
        long downtimeWeek = getTotalDowntime(mutatorsOfServersWeek);
        long downtimeDay = getTotalDowntime(mutatorsOfServersDay);
        numbers.put("server_downtime_30d", downtimeMonth);
        numbers.put("server_downtime_7d", downtimeWeek);
        numbers.put("server_downtime_24h", downtimeDay);

        if (!tpsData.isEmpty()) {
            numbers.put("avg_server_downtime_30d", downtimeMonth / tpsData.size());
            numbers.put("avg_server_downtime_7d", downtimeWeek / tpsData.size());
            numbers.put("avg_server_downtime_24h", downtimeDay / tpsData.size());
        } else {
            numbers.put("avg_server_downtime_30d", "-");
            numbers.put("avg_server_downtime_7d", "-");
            numbers.put("avg_server_downtime_24h", "-");
        }

        numbers.put("players_30d", format(tpsDataMonth.averagePlayers()));
        numbers.put("players_7d", format(tpsDataWeek.averagePlayers()));
        numbers.put("players_24h", format(tpsDataDay.averagePlayers()));
        numbers.put("tps_30d", format(tpsDataMonth.averageTPS()));
        numbers.put("tps_7d", format(tpsDataWeek.averageTPS()));
        numbers.put("tps_24h", format(tpsDataDay.averageTPS()));
        numbers.put("cpu_30d", formatPercentage(tpsDataMonth.averageCPU()));
        numbers.put("cpu_7d", formatPercentage(tpsDataWeek.averageCPU()));
        numbers.put("cpu_24h", formatPercentage(tpsDataDay.averageCPU()));
        numbers.put("ram_30d", formatBytes(tpsDataMonth.averageRAM()));
        numbers.put("ram_7d", formatBytes(tpsDataWeek.averageRAM()));
        numbers.put("ram_24h", formatBytes(tpsDataDay.averageRAM()));
        numbers.put("entities_30d", format((int) tpsDataMonth.averageEntities()));
        numbers.put("entities_7d", format((int) tpsDataWeek.averageEntities()));
        numbers.put("entities_24h", format((int) tpsDataDay.averageEntities()));
        numbers.put("chunks_30d", format((int) tpsDataMonth.averageChunks()));
        numbers.put("chunks_7d", format((int) tpsDataWeek.averageChunks()));
        numbers.put("chunks_24h", format((int) tpsDataDay.averageChunks()));

        return numbers;
    }

    private long getTotalDowntime(Map<Integer, TPSMutator> mutatorsOfServersMonth) {
        long downTime = 0L;
        for (TPSMutator tpsMutator : mutatorsOfServersMonth.values()) {
            downTime += tpsMutator.serverDownTime();
        }
        return downTime;
    }

    private String format(double value) {
        return value != -1 ? decimals.apply(value) : GenericLang.UNAVAILABLE.getKey();
    }

    private String formatBytes(double value) {
        return value != -1 ? byteSize.apply(value) : GenericLang.UNAVAILABLE.getKey();
    }

    private String formatPercentage(double value) {
        return value != -1 ? percentage.apply(value / 100.0) : GenericLang.UNAVAILABLE.getKey();
    }

}