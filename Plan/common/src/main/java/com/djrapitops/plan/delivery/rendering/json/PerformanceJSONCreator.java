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
package com.djrapitops.plan.delivery.rendering.json;

import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Creates JSON payload for /server-page Performance tab.
 *
 * @author AuroraLS3
 */
@Singleton
public class PerformanceJSONCreator implements ServerTabJSONCreator<Map<String, Object>> {

    private final PlanConfig config;
    private final DBSystem dbSystem;

    private final Formatter<Double> decimals;
    private final Formatter<Double> percentage;
    private final Formatter<Double> byteSize;

    @Inject
    public PerformanceJSONCreator(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        decimals = formatters.decimals();
        percentage = formatters.percentage();
        byteSize = formatters.byteSize();
    }

    @Override
    public Map<String, Object> createJSONAsMap(ServerUUID serverUUID) {
        Map<String, Object> serverOverview = new HashMap<>();
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        List<TPS> tpsData = db.query(TPSQueries.fetchTPSDataOfServer(monthAgo, now, serverUUID));

        serverOverview.put("numbers", createNumbersMap(tpsData));
        serverOverview.put("insights", createInsightsMap(tpsData));
        return serverOverview;
    }

    private Map<String, Object> createNumbersMap(List<TPS> tpsData) {
        long now = System.currentTimeMillis();
        long dayAgo = now - TimeUnit.DAYS.toMillis(1L);
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);

        Map<String, Object> numbers = new HashMap<>();

        TPSMutator tpsDataMonth = new TPSMutator(tpsData);
        TPSMutator tpsDataWeek = tpsDataMonth.filterDataBetween(weekAgo, now);
        TPSMutator tpsDataDay = tpsDataWeek.filterDataBetween(dayAgo, now);

        Double tpsThreshold = config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED);
        numbers.put("low_tps_spikes_30d", tpsDataMonth.lowTpsSpikeCount(tpsThreshold));
        numbers.put("low_tps_spikes_7d", tpsDataWeek.lowTpsSpikeCount(tpsThreshold));
        numbers.put("low_tps_spikes_24h", tpsDataDay.lowTpsSpikeCount(tpsThreshold));

        numbers.put("server_downtime_30d", tpsDataMonth.serverDownTime());
        numbers.put("server_downtime_7d", tpsDataWeek.serverDownTime());
        numbers.put("server_downtime_24h", tpsDataDay.serverDownTime());

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

        numbers.put("max_disk_30d", formatBytes(tpsDataMonth.maxFreeDisk()));
        numbers.put("max_disk_7d", formatBytes(tpsDataWeek.maxFreeDisk()));
        numbers.put("max_disk_24h", formatBytes(tpsDataDay.maxFreeDisk()));
        numbers.put("min_disk_30d", formatBytes(tpsDataMonth.minFreeDisk()));
        numbers.put("min_disk_7d", formatBytes(tpsDataWeek.minFreeDisk()));
        numbers.put("min_disk_24h", formatBytes(tpsDataDay.minFreeDisk()));

        return numbers;
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

    private Map<String, Object> createInsightsMap(List<TPS> tpsData) {
        TPSMutator tpsMutator = new TPSMutator(tpsData);
        Double tpsThreshold = config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED);
        TPSMutator lowTPS = tpsMutator.filterTPSBetween(-1, tpsThreshold);

        Map<String, Object> insights = new HashMap<>();

        double averageTPS = lowTPS.averageTPS();
        double avgPlayersOnline = lowTPS.averagePlayersOnline();
        double averageCPU = lowTPS.averageCPU();
        double averageEntities = lowTPS.averageEntities();
        double averageChunks = lowTPS.averageChunks();
        insights.put("low_tps_players", avgPlayersOnline != -1 ? decimals.apply(avgPlayersOnline) : HtmlLang.TEXT_NO_LOW_TPS.getKey());
        insights.put("low_tps_tps", averageTPS != -1 ? decimals.apply(averageTPS) : "-");
        insights.put("low_tps_cpu", averageCPU != -1 ? decimals.apply(averageCPU) : "-");
        insights.put("low_tps_entities", averageEntities != -1 ? decimals.apply(averageEntities) : "-");
        insights.put("low_tps_chunks", averageChunks != -1 ? decimals.apply(averageChunks) : "-");

        return insights;
    }
}