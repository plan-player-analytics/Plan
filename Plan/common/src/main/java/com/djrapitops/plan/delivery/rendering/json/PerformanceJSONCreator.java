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
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import net.playeranalytics.plugin.server.PluginLogger;

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
 * @deprecated Use /v1/datapoint instead (types TPS_LOW_SPIKES, UPTIME, DOWNTIME, TPS_AVERAGE, PLAYERS_ONLINE_AVERAGE, MSPT_AVERAGE, CPU_AVERAGE, RAM_AVERAGE, ENTITIES_AVERAGE, CHUNKS_AVERAGE, DISK_MAX, DISK_MIN).
 *
 */
@Singleton
@Deprecated(since = "2026-05-24 / 5.7 build 3460")
public class PerformanceJSONCreator implements ServerTabJSONCreator<Map<String, Object>> {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Locale locale;
    private final PluginLogger logger;

    private final Formatter<Double> decimals;
    private final Formatter<Double> percentage;
    private final Formatter<Double> byteSize;

    @Inject
    public PerformanceJSONCreator(
            PlanConfig config,
            DBSystem dbSystem, Locale locale, PluginLogger logger,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.locale = locale;
        this.logger = logger;

        decimals = formatters.decimals();
        percentage = formatters.percentage();
        byteSize = formatters.byteSize();
    }

    @Override
    public Map<String, Object> createJSONAsMap(ServerUUID serverUUID) {
        logger.warn(locale.getString(PluginLang.DEPRECATED_ENDPOINT_CALL, "/v1/performanceOverview", "/v1/datapoint"));

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
        numbers.put("lowTpsSpikes30d", tpsDataMonth.lowTpsSpikeCount(tpsThreshold));
        numbers.put("lowTpsSpikes7d", tpsDataWeek.lowTpsSpikeCount(tpsThreshold));
        numbers.put("lowTpsSpikes24h", tpsDataDay.lowTpsSpikeCount(tpsThreshold));

        numbers.put("serverDowntime30d", tpsDataMonth.serverDownTime());
        numbers.put("serverDowntime7d", tpsDataWeek.serverDownTime());
        numbers.put("serverDowntime24h", tpsDataDay.serverDownTime());

        numbers.put("serverUptime30d", tpsDataMonth.serverUptime());
        numbers.put("serverUptime7d", tpsDataWeek.serverUptime());
        numbers.put("serverUptime24h", tpsDataDay.serverUptime());

        numbers.put("players30d", tpsDataMonth.averagePlayers());
        numbers.put("players7d", tpsDataWeek.averagePlayers());
        numbers.put("players24h", tpsDataDay.averagePlayers());
        numbers.put("tps30d", tpsDataMonth.averageTPS());
        numbers.put("tps7d", tpsDataWeek.averageTPS());
        numbers.put("tps24h", tpsDataDay.averageTPS());
        numbers.put("cpu30d", formatPercentage(tpsDataMonth.averageCPU()));
        numbers.put("cpu7d", formatPercentage(tpsDataWeek.averageCPU()));
        numbers.put("cpu24h", formatPercentage(tpsDataDay.averageCPU()));
        numbers.put("ram30d", formatBytes(tpsDataMonth.averageRAM()));
        numbers.put("ram7d", formatBytes(tpsDataWeek.averageRAM()));
        numbers.put("ram24h", formatBytes(tpsDataDay.averageRAM()));
        numbers.put("entities30d", (int) tpsDataMonth.averageEntities());
        numbers.put("entities7d", (int) tpsDataWeek.averageEntities());
        numbers.put("entities24h", (int) tpsDataDay.averageEntities());
        numbers.put("chunks30d", (int) tpsDataMonth.averageChunks());
        numbers.put("chunks7d", (int) tpsDataWeek.averageChunks());
        numbers.put("chunks24h", (int) tpsDataDay.averageChunks());

        numbers.put("maxDisk30d", formatBytes(tpsDataMonth.maxFreeDisk()));
        numbers.put("maxDisk7d", formatBytes(tpsDataWeek.maxFreeDisk()));
        numbers.put("maxDisk24h", formatBytes(tpsDataDay.maxFreeDisk()));
        numbers.put("minDisk30d", formatBytes(tpsDataMonth.minFreeDisk()));
        numbers.put("minDisk7d", formatBytes(tpsDataWeek.minFreeDisk()));
        numbers.put("minDisk24h", formatBytes(tpsDataDay.minFreeDisk()));

        numbers.put("msptAverage30d", tpsDataMonth.averageMspt());
        numbers.put("msptAverage7d", tpsDataWeek.averageMspt());
        numbers.put("msptAverage24h", tpsDataDay.averageMspt());

        return numbers;
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
        double averageMspt = lowTPS.averageMspt();
        insights.put("lowTpsPlayers", avgPlayersOnline != -1 ? decimals.apply(avgPlayersOnline) : HtmlLang.TEXT_NO_LOW_TPS.getKey());
        insights.put("lowTpsTps", averageTPS != -1 ? decimals.apply(averageTPS) : "-");
        insights.put("lowTpsCpu", averageCPU != -1 ? decimals.apply(averageCPU) : "-");
        insights.put("lowTpsEntities", averageEntities != -1 ? decimals.apply(averageEntities) : "-");
        insights.put("lowTpsChunks", averageChunks != -1 ? decimals.apply(averageChunks) : "-");
        insights.put("lowTpsMspt", averageMspt > 0 ? decimals.apply(averageMspt) : "-");

        return insights;
    }
}