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
package com.djrapitops.plan.system.delivery.rendering.json;

import com.djrapitops.plan.system.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.system.gathering.domain.TPS;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Parses JSON payload for /server-page Performance tab.
 *
 * @author Rsl1122
 */
@Singleton
public class PerformanceJSONParser implements ServerTabJSONParser<Map<String, Object>> {

    private final PlanConfig config;
    private final DBSystem dbSystem;

    private final Formatter<Double> decimals;
    private final Formatter<Long> timeAmountFormatter;
    private final Formatter<Double> percentageFormatter;

    @Inject
    public PerformanceJSONParser(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        decimals = formatters.decimals();
        percentageFormatter = formatters.percentage();
        timeAmountFormatter = formatters.timeAmount();
    }

    public Map<String, Object> createJSONAsMap(UUID serverUUID) {
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

        Integer tpsThreshold = config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED);
        numbers.put("low_tps_spikes_30d", tpsDataMonth.lowTpsSpikeCount(tpsThreshold));
        numbers.put("low_tps_spikes_7d", tpsDataWeek.lowTpsSpikeCount(tpsThreshold));
        numbers.put("low_tps_spikes_24h", tpsDataDay.lowTpsSpikeCount(tpsThreshold));

        numbers.put("server_downtime_30d", timeAmountFormatter.apply(tpsDataMonth.serverDownTime()));
        numbers.put("server_downtime_7d", timeAmountFormatter.apply(tpsDataWeek.serverDownTime()));
        numbers.put("server_downtime_24h", timeAmountFormatter.apply(tpsDataDay.serverDownTime()));

        numbers.put("tps_30d", format(tpsDataMonth.averageTPS()));
        numbers.put("tps_7d", format(tpsDataWeek.averageTPS()));
        numbers.put("tps_24h", format(tpsDataDay.averageTPS()));
        numbers.put("cpu_30d", percentageFormatter.apply(tpsDataMonth.averageCPU() / 100.0));
        numbers.put("cpu_7d", percentageFormatter.apply(tpsDataWeek.averageCPU() / 100.0));
        numbers.put("cpu_24h", percentageFormatter.apply(tpsDataDay.averageCPU() / 100.0));
        numbers.put("ram_30d", format(tpsDataMonth.averageRAM(), " MB"));
        numbers.put("ram_7d", format(tpsDataWeek.averageRAM(), " MB"));
        numbers.put("ram_24h", format(tpsDataDay.averageRAM(), " MB"));
        numbers.put("entities_30d", format((int) tpsDataMonth.averageEntities()));
        numbers.put("entities_7d", format((int) tpsDataWeek.averageEntities()));
        numbers.put("entities_24h", format((int) tpsDataDay.averageEntities()));
        numbers.put("chunks_30d", format((int) tpsDataMonth.averageChunks()));
        numbers.put("chunks_7d", format((int) tpsDataWeek.averageChunks()));
        numbers.put("chunks_24h", format((int) tpsDataDay.averageChunks()));

        numbers.put("max_disk_30d", format(tpsDataMonth.maxFreeDisk(), " Mb"));
        numbers.put("max_disk_7d", format(tpsDataWeek.maxFreeDisk(), " Mb"));
        numbers.put("max_disk_24h", format(tpsDataDay.maxFreeDisk(), " Mb"));
        numbers.put("min_disk_30d", format(tpsDataMonth.minFreeDisk(), " Mb"));
        numbers.put("min_disk_7d", format(tpsDataWeek.minFreeDisk(), " Mb"));
        numbers.put("min_disk_24h", format(tpsDataDay.minFreeDisk(), " Mb"));

        return numbers;
    }

    private String format(double value) {
        return value != -1 ? decimals.apply(value) : "Unavailable";
    }

    private String format(double value, String suffix) {
        return value != -1 ? decimals.apply(value) + suffix : "Unavailable";
    }

    private Map<String, Object> createInsightsMap(List<TPS> tpsData) {
        TPSMutator tpsMutator = new TPSMutator(tpsData);
        Integer tpsThreshold = config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED);
        TPSMutator lowTPS = tpsMutator.filterTPSBetween(-1, tpsThreshold);

        Map<String, Object> insights = new HashMap<>();

        double avgPlayersOnline = lowTPS.averagePlayersOnline();
        double averageCPU = lowTPS.averageCPU();
        double averageEntities = lowTPS.averageEntities();
        double averageChunks = lowTPS.averageChunks();
        insights.put("low_tps_players", avgPlayersOnline != -1 ? decimals.apply(avgPlayersOnline) : "No low tps spikes");
        insights.put("low_tps_cpu", averageCPU != -1 ? decimals.apply(averageCPU) : "-");
        insights.put("low_tps_entities", averageEntities != -1 ? decimals.apply(averageEntities) : "-");
        insights.put("low_tps_chunks", averageChunks != -1 ? decimals.apply(averageChunks) : "-");

        return insights;
    }
}