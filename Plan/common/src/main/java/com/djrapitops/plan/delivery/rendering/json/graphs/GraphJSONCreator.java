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
package com.djrapitops.plan.delivery.rendering.json.graphs;

import com.djrapitops.plan.delivery.domain.DateMap;
import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.JoinAddressCount;
import com.djrapitops.plan.delivery.domain.JoinAddressCounts;
import com.djrapitops.plan.delivery.domain.datatransfer.ServerDto;
import com.djrapitops.plan.delivery.domain.datatransfer.graphs.GraphCollection;
import com.djrapitops.plan.delivery.domain.datatransfer.graphs.ServerSpecificLineGraph;
import com.djrapitops.plan.delivery.domain.mutators.MutatorFunctions;
import com.djrapitops.plan.delivery.domain.mutators.PingMutator;
import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.delivery.rendering.json.graphs.bar.BarGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.LineGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.LineGraphFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.PingGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.Pie;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.WorldPie;
import com.djrapitops.plan.delivery.rendering.json.graphs.special.WorldMap;
import com.djrapitops.plan.delivery.rendering.json.graphs.stack.StackGraph;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.utilities.comparators.DateHolderOldestComparator;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.java.Maps;
import net.playeranalytics.plugin.scheduling.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Creates Graph related Data JSON.
 *
 * @author AuroraLS3
 */
@Singleton
public class GraphJSONCreator {

    private final PlanConfig config;
    private final Locale locale;
    private final Theme theme;
    private final DBSystem dbSystem;
    private final Graphs graphs;

    @Inject
    public GraphJSONCreator(
            PlanConfig config,
            Locale locale,
            Theme theme,
            DBSystem dbSystem,
            Graphs graphs
    ) {
        this.config = config;
        this.locale = locale;
        this.theme = theme;
        this.dbSystem = dbSystem;
        this.graphs = graphs;
    }

    public String performanceGraphJSON(ServerUUID serverUUID) {
        long now = System.currentTimeMillis();
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180);
        TPSMutator tpsMutator = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServer(halfYearAgo, now, serverUUID)));
        return '{' +
                "\"playersOnline\":" + lineGraphs.playersOnlineGraph(tpsMutator).toHighChartsSeries() +
                ",\"tps\":" + lineGraphs.tpsGraph(tpsMutator).toHighChartsSeries() +
                ",\"cpu\":" + lineGraphs.cpuGraph(tpsMutator).toHighChartsSeries() +
                ",\"ram\":" + lineGraphs.ramGraph(tpsMutator).toHighChartsSeries() +
                ",\"entities\":" + lineGraphs.entityGraph(tpsMutator).toHighChartsSeries() +
                ",\"chunks\":" + lineGraphs.chunkGraph(tpsMutator).toHighChartsSeries() +
                ",\"disk\":" + lineGraphs.diskGraph(tpsMutator).toHighChartsSeries() +
                ",\"colors\":{" +
                "\"playersOnline\":\"" + theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE) + "\"," +
                "\"cpu\":\"" + theme.getValue(ThemeVal.GRAPH_CPU) + "\"," +
                "\"ram\":\"" + theme.getValue(ThemeVal.GRAPH_RAM) + "\"," +
                "\"entities\":\"" + theme.getValue(ThemeVal.GRAPH_ENTITIES) + "\"," +
                "\"chunks\":\"" + theme.getValue(ThemeVal.GRAPH_CHUNKS) + "\"," +
                "\"low\":\"" + theme.getValue(ThemeVal.GRAPH_TPS_LOW) + "\"," +
                "\"med\":\"" + theme.getValue(ThemeVal.GRAPH_TPS_MED) + "\"," +
                "\"high\":\"" + theme.getValue(ThemeVal.GRAPH_TPS_HIGH) + "\"}" +
                ",\"zones\":{" +
                "\"tpsThresholdMed\":" + config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED) + ',' +
                "\"tpsThresholdHigh\":" + config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_HIGH) + ',' +
                "\"diskThresholdMed\":" + config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_MED) + ',' +
                "\"diskThresholdHigh\":" + config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_HIGH) +
                "}}";
    }

    public Map<String, Object> optimizedPerformanceGraphJSON(ServerUUID serverUUID) {
        long now = System.currentTimeMillis();
        long twoMonthsAgo = now - TimeUnit.DAYS.toMillis(60);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30);

        long lowestResolution = TimeUnit.MINUTES.toMillis(20);
        long lowResolution = TimeUnit.MINUTES.toMillis(5);
        Database db = dbSystem.getDatabase();
        TPSMutator lowestResolutionData = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServerInResolution(0, twoMonthsAgo, lowestResolution, serverUUID)));
        TPSMutator lowResolutionData = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServerInResolution(twoMonthsAgo, monthAgo, lowResolution, serverUUID)));
        TPSMutator highResolutionData = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServer(monthAgo, now, serverUUID)));

        String serverName = db.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID))
                .map(Server::getIdentifiableName)
                .orElse(serverUUID.toString());

        List<Number[]> values = lowestResolutionData.toArrays(new LineGraph.GapStrategy(
                config.isTrue(DisplaySettings.GAPS_IN_GRAPH_DATA),
                lowestResolution + TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(30),
                null
        ));
        values.addAll(lowResolutionData.toArrays(new LineGraph.GapStrategy(
                config.isTrue(DisplaySettings.GAPS_IN_GRAPH_DATA),
                lowResolution + TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(30),
                null
        )));
        values.addAll(highResolutionData.toArrays(new LineGraph.GapStrategy(
                config.isTrue(DisplaySettings.GAPS_IN_GRAPH_DATA),
                TimeUnit.MINUTES.toMillis(3),
                TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(30),
                null
        )));

        return Maps.builder(String.class, Object.class)
                .put("keys", new String[]{"date", "playersOnline", "tps", "cpu", "ram", "entities", "chunks", "disk"})
                .put("values", values)
                .put("colors", Maps.builder(String.class, Object.class)
                        .put("playersOnline", theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE))
                        .put("cpu", theme.getValue(ThemeVal.GRAPH_CPU))
                        .put("ram", theme.getValue(ThemeVal.GRAPH_RAM))
                        .put("entities", theme.getValue(ThemeVal.GRAPH_ENTITIES))
                        .put("chunks", theme.getValue(ThemeVal.GRAPH_CHUNKS))
                        .put("low", theme.getValue(ThemeVal.GRAPH_TPS_LOW))
                        .put("med", theme.getValue(ThemeVal.GRAPH_TPS_MED))
                        .put("high", theme.getValue(ThemeVal.GRAPH_TPS_HIGH))
                        .build())
                .put("zones", Maps.builder(String.class, Object.class)
                        .put("tpsThresholdMed", config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED))
                        .put("tpsThresholdHigh", config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_HIGH))
                        .put("diskThresholdMed", config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_MED))
                        .put("diskThresholdHigh", config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_HIGH))
                        .build())
                .put("serverName", serverName)
                .put("serverUUID", serverUUID)
                .build();
    }

    public String playersOnlineGraph(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);

        List<Point> points = Lists.map(
                db.query(TPSQueries.fetchPlayersOnlineOfServer(halfYearAgo, now, serverUUID)),
                Point::fromDateObj
        );
        return "{\"playersOnline\":" + graphs.line().lineGraph(points).toHighChartsSeries() +
                ",\"color\":\"" + theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE) + "\"}";
    }

    public String uniqueAndNewGraphJSON(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(halfYearAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(halfYearAgo, now, timeZoneOffset, serverUUID)
        );

        return createUniqueAndNewJSON(lineGraphs, uniquePerDay, newPerDay, TimeUnit.DAYS.toMillis(1L));
    }

    public String hourlyUniqueAndNewGraphJSON(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.hourlyUniquePlayerCounts(weekAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(weekAgo, now, timeZoneOffset, serverUUID)
        );

        return createUniqueAndNewJSON(lineGraphs, uniquePerDay, newPerDay, TimeUnit.HOURS.toMillis(1L));
    }

    public String createUniqueAndNewJSON(LineGraphFactory lineGraphs, NavigableMap<Long, Integer> uniquePerDay, NavigableMap<Long, Integer> newPerDay, long gapFillPeriod) {
        LineGraph.GapStrategy gapStrategy = new LineGraph.GapStrategy(true, gapFillPeriod, 0, gapFillPeriod, 0.0);
        return "{\"uniquePlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPoints(
                        MutatorFunctions.addMissing(uniquePerDay, gapFillPeriod, 0)
                ), gapStrategy).toHighChartsSeries() +
                ",\"newPlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPoints(
                        MutatorFunctions.addMissing(newPerDay, gapFillPeriod, 0)
                ), gapStrategy).toHighChartsSeries() +
                ",\"colors\":{" +
                "\"playersOnline\":\"" + theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE) + "\"," +
                "\"newPlayers\":\"" + theme.getValue(ThemeVal.LIGHT_GREEN) + "\"" +
                "}}";
    }

    public String uniqueAndNewGraphJSON() {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(halfYearAgo, now, timeZoneOffset)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(halfYearAgo, now, timeZoneOffset)
        );

        return createUniqueAndNewJSON(lineGraphs, uniquePerDay, newPerDay, TimeUnit.DAYS.toMillis(1L));
    }

    public String hourlyUniqueAndNewGraphJSON() {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.hourlyUniquePlayerCounts(weekAgo, now, timeZoneOffset)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.hourlyNewPlayerCounts(weekAgo, now, timeZoneOffset)
        );

        return createUniqueAndNewJSON(lineGraphs, uniquePerDay, newPerDay, TimeUnit.HOURS.toMillis(1L));
    }

    public String serverCalendarJSON(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long twoYearsAgo = now - TimeUnit.DAYS.toMillis(730L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(twoYearsAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(twoYearsAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Long> playtimePerDay = db.query(
                SessionQueries.playtimePerDay(twoYearsAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Integer> sessionsPerDay = db.query(
                SessionQueries.sessionCountPerDay(twoYearsAgo, now, timeZoneOffset, serverUUID)
        );
        return "{\"data\":" +
                graphs.calendar().serverCalendar(
                        uniquePerDay,
                        newPerDay,
                        playtimePerDay,
                        sessionsPerDay
                ).toCalendarSeries() +
                ",\"firstDay\":" + 1 + '}';
    }

    public String networkCalendarJSON() {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long twoYearsAgo = now - TimeUnit.DAYS.toMillis(730L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(twoYearsAgo, now, timeZoneOffset)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(twoYearsAgo, now, timeZoneOffset)
        );
        NavigableMap<Long, Long> playtimePerDay = db.query(
                SessionQueries.playtimePerDay(twoYearsAgo, now, timeZoneOffset)
        );
        NavigableMap<Long, Integer> sessionsPerDay = db.query(
                SessionQueries.sessionCountPerDay(twoYearsAgo, now, timeZoneOffset)
        );
        return "{\"data\":" +
                graphs.calendar().serverCalendar(
                        uniquePerDay,
                        newPerDay,
                        playtimePerDay,
                        sessionsPerDay
                ).toCalendarSeries() +
                ",\"firstDay\":" + 1 + '}';
    }

    public Map<String, Object> serverWorldPieJSONAsMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        WorldTimes worldTimes = db.query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID));
        WorldPie worldPie = graphs.pie().worldPie(worldTimes);

        return Maps.builder(String.class, Object.class)
                .put("world_series", worldPie.getSlices())
                .put("gm_series", worldPie.toHighChartsDrillDownMaps())
                .build();
    }

    public Map<String, Object> activityGraphsJSONAsMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long date = System.currentTimeMillis();
        Long threshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        DateMap<Map<String, Integer>> activityData = new DateMap<>();
        for (long time = date; time >= date - TimeAmount.MONTH.toMillis(2L); time -= TimeAmount.WEEK.toMillis(1L)) {
            activityData.put(time, db.query(ActivityIndexQueries.fetchActivityIndexGroupingsOn(time, serverUUID, threshold)));
        }

        return createActivityGraphJSON(activityData);
    }

    public Map<String, Object> createActivityGraphJSON(DateMap<Map<String, Integer>> activityData) {
        Map.Entry<Long, Map<String, Integer>> lastActivityEntry = activityData.lastEntry();
        Pie activityPie = graphs.pie().activityPie(lastActivityEntry != null ? lastActivityEntry.getValue() : Collections.emptyMap());
        StackGraph activityStackGraph = graphs.stack().activityStackGraph(activityData);

        return Maps.builder(String.class, Object.class)
                .put("activity_series", activityStackGraph.getDataSets())
                .put("activity_labels", activityStackGraph.getLabels())
                .put("activity_pie_series", activityPie.getSlices())
                .build();
    }

    public Map<String, Object> activityGraphsJSONAsMap() {
        Database db = dbSystem.getDatabase();
        long date = System.currentTimeMillis();
        Long threshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        DateMap<Map<String, Integer>> activityData = new DateMap<>();
        for (long time = date; time >= date - TimeAmount.MONTH.toMillis(2L); time -= TimeAmount.WEEK.toMillis(1L)) {
            activityData.put(time, db.query(NetworkActivityIndexQueries.fetchActivityIndexGroupingsOn(time, threshold)));
        }

        return createActivityGraphJSON(activityData);
    }

    public Map<String, Object> geolocationGraphsJSONAsMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        Map<String, Integer> geolocationCounts = db.query(GeoInfoQueries.serverGeolocationCounts(serverUUID));

        return createGeolocationJSON(geolocationCounts);
    }

    public Map<String, Object> createGeolocationJSON(Map<String, Integer> geolocationCounts) {
        BarGraph geolocationBarGraph = graphs.bar().geolocationBarGraph(geolocationCounts);
        WorldMap worldMap = graphs.special().worldMap(geolocationCounts);

        return Maps.builder(String.class, Object.class)
                .put("geolocations_enabled", config.get(DataGatheringSettings.GEOLOCATIONS) && config.get(DataGatheringSettings.ACCEPT_GEOLITE2_EULA))
                .put("geolocation_series", worldMap.getEntries())
                .put("geolocation_bar_series", geolocationBarGraph.getBars())
                .put("colors", Maps.builder(String.class, String.class)
                        .put("low", theme.getValue(ThemeVal.WORLD_MAP_LOW))
                        .put("high", theme.getValue(ThemeVal.WORLD_MAP_HIGH))
                        .put("bars", theme.getValue(ThemeVal.GREEN))
                        .build())
                .build();
    }

    public Map<String, Object> geolocationGraphsJSONAsMap() {
        Database db = dbSystem.getDatabase();
        Map<String, Integer> geolocationCounts = db.query(GeoInfoQueries.networkGeolocationCounts());

        return createGeolocationJSON(geolocationCounts);
    }

    public String pingGraphsJSON(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        List<Ping> pings = db.query(PingQueries.fetchPingDataOfServer(now - TimeUnit.DAYS.toMillis(180L), now, serverUUID));

        PingGraph pingGraph = graphs.line().pingGraph(new PingMutator(pings).mutateToByMinutePings().all());

        return "{\"min_ping_series\":" + pingGraph.getMinGraph().toHighChartsSeries() +
                ",\"avg_ping_series\":" + pingGraph.getAvgGraph().toHighChartsSeries() +
                ",\"max_ping_series\":" + pingGraph.getMaxGraph().toHighChartsSeries() +
                ",\"colors\":{" +
                "\"min\":\"" + theme.getValue(ThemeVal.GRAPH_MIN_PING) + "\"," +
                "\"avg\":\"" + theme.getValue(ThemeVal.GRAPH_AVG_PING) + "\"," +
                "\"max\":\"" + theme.getValue(ThemeVal.GRAPH_MAX_PING) + "\"" +
                "}}";
    }

    public Map<String, Object> punchCardJSONAsMap(ServerUUID serverUUID) {
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        List<FinishedSession> sessions = dbSystem.getDatabase().query(
                SessionQueries.fetchServerSessionsWithoutKillOrWorldData(monthAgo, now, serverUUID)
        );
        return Maps.builder(String.class, Object.class)
                .put("punchCard", graphs.special().punchCard(sessions).getDots())
                .put("color", theme.getValue(ThemeVal.GRAPH_PUNCHCARD))
                .build();
    }

    public Map<String, Object> serverPreferencePieJSONAsMap() {
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        String[] pieColors = theme.getPieColors(ThemeVal.GRAPH_WORLD_PIE);
        Map<String, Long> playtimePerServer = dbSystem.getDatabase().query(SessionQueries.playtimePerServer(monthAgo, now));

        return Maps.builder(String.class, Object.class)
                .put("server_pie_colors", pieColors)
                .put("server_pie_series_30d", graphs.pie().serverPreferencePie(playtimePerServer).getSlices())
                .build();
    }

    public void translateUnknown(Map<String, Integer> joinAddresses) {
        Integer unknown = joinAddresses.get(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        if (unknown != null) {
            joinAddresses.remove(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
            joinAddresses.put(GenericLang.UNKNOWN.getKey(), unknown);
        }
    }

    public Map<String, Object> joinAddressesByDay(ServerUUID serverUUID, long after, long before, @Untrusted List<String> addressFilter) {
        String[] pieColors = theme.getPieColors(ThemeVal.GRAPH_WORLD_PIE);
        List<DateObj<Map<String, Integer>>> joinAddresses = dbSystem.getDatabase().query(JoinAddressQueries.joinAddressesPerDay(serverUUID, config.getTimeZone().getOffset(System.currentTimeMillis()), after, before, addressFilter));

        return mapToJson(pieColors, joinAddresses);
    }

    public Map<String, Object> joinAddressesByDay(long after, long before, @Untrusted List<String> addressFilter) {
        String[] pieColors = theme.getPieColors(ThemeVal.GRAPH_WORLD_PIE);
        List<DateObj<Map<String, Integer>>> joinAddresses = dbSystem.getDatabase().query(JoinAddressQueries.joinAddressesPerDay(config.getTimeZone().getOffset(System.currentTimeMillis()), after, before, addressFilter));

        return mapToJson(pieColors, joinAddresses);
    }

    private static void removeFilteredAddresses(List<JoinAddressCount> addresses, List<String> filteredJoinAddresses) {
        if (filteredJoinAddresses.isEmpty() || filteredJoinAddresses.equals(List.of("play.example.com"))) return;

        List<JoinAddressCount> addressesToRemove = addresses.stream()
                .filter(address -> filteredJoinAddresses.contains(address.getJoinAddress()))
                .collect(Collectors.toList());

        if (!addressesToRemove.isEmpty()) {
            Optional<JoinAddressCount> foundUnknownAddressCount = addresses.stream()
                    .filter(address -> address.getJoinAddress().equals(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP))
                    .findFirst();
            JoinAddressCount unknownAddressCount;
            if (foundUnknownAddressCount.isEmpty()) {
                unknownAddressCount = new JoinAddressCount(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP, 0);
                addresses.add(unknownAddressCount);
            } else {
                unknownAddressCount = foundUnknownAddressCount.get();
            }

            for (JoinAddressCount toRemove : addressesToRemove) {
                unknownAddressCount.setCount(unknownAddressCount.getCount() + toRemove.getCount());
                addresses.remove(toRemove);
            }
        }
    }

    private Map<String, Object> mapToJson(String[] pieColors, List<DateObj<Map<String, Integer>>> joinAddresses) {
        for (DateObj<Map<String, Integer>> addressesByDate : joinAddresses) {
            translateUnknown(addressesByDate.getValue());
        }

        List<String> filteredJoinAddresses = config.get(DataGatheringSettings.FILTER_JOIN_ADDRESSES);

        List<JoinAddressCounts> joinAddressCounts = joinAddresses.stream()
                .map(addressesOnDay -> {
                    List<JoinAddressCount> addresses = addressesOnDay.getValue().entrySet()
                            .stream()
                            .map(JoinAddressCount::new)
                            .sorted()
                            .collect(Collectors.toList());

                    removeFilteredAddresses(addresses, filteredJoinAddresses);

                    return new JoinAddressCounts(addressesOnDay.getDate(), addresses);
                })
                .sorted(new DateHolderOldestComparator())
                .collect(Collectors.toList());

        return Maps.builder(String.class, Object.class)
                .put("colors", pieColors)
                .put("join_addresses_by_date", joinAddressCounts)
                .build();
    }

    public GraphCollection<ServerSpecificLineGraph> proxyPlayersOnlineGraphs() {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);

        List<ServerSpecificLineGraph> proxyGraphs = new ArrayList<>();
        for (Server proxy : db.query(ServerQueries.fetchProxyServers())) {
            ServerUUID proxyUUID = proxy.getUuid();
            List<Number[]> points = Lists.map(
                    db.query(TPSQueries.fetchPlayersOnlineOfServer(halfYearAgo, now, proxyUUID)),
                    point -> Point.fromDateObj(point).toArray()
            );
            proxyGraphs.add(new ServerSpecificLineGraph(points, ServerDto.fromServer(proxy)));
        }

        return new GraphCollection<>(proxyGraphs, theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
    }
}