package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.comparators.PieSliceComparator;

import java.util.*;
import java.util.stream.Collectors;

public class WorldPie extends AbstractPieChartWithDrilldown {

    private WorldTimes worldTimes;

    public WorldPie(WorldTimes worldTimes) {
        super(turnIntoSlices(worldTimes));

        this.worldTimes = worldTimes;

        if (Settings.ORDER_WORLD_PIE_BY_PERC.isTrue()) {
            slices.sort(new PieSliceComparator());
        }
    }

    private static List<PieSlice> turnIntoSlices(WorldTimes worldTimes) {
        String[] colors = Theme.getValue(ThemeVal.GRAPH_WORLD_PIE).split(", ");
        int colLenght = colors.length;

        // WorldTimes Map<String, GMTimes> (GMTimes.getTotal)
        Map<String, Long> playtimePerWorld = worldTimes.getWorldTimes().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTotal()));

        Map<String, Long> playtimePerAlias = transformToAliases(playtimePerWorld);

        List<String> worlds = new ArrayList<>(playtimePerAlias.keySet());
        Collections.sort(worlds);

        List<PieSlice> slices = new ArrayList<>();
        int i = 0;
        for (String alias : worlds) {
            Long value = playtimePerAlias.getOrDefault(alias, 0L);
            if (value != 0L) {
                slices.add(new PieSlice(alias, value, colors[i % colLenght], true));
            }
            i++;
        }

        return slices;
    }

    private static Map<String, Long> transformToAliases(Map<String, Long> playtimePerWorld) {
        WorldAliasSettings aliasSettings = new WorldAliasSettings();
        Map<String, String> aliases = aliasSettings.getAliases();
        return transformToAliases(playtimePerWorld, aliases);
    }

    // TODO Move to another class
    public static Map<String, Long> transformToAliases(Map<String, Long> playtimePerWorld, Map<String, String> aliases) {
        // TODO Optimization is possible (WorldAliasSettings)
        WorldAliasSettings aliasSettings = new WorldAliasSettings();

        Map<String, Long> playtimePerAlias = new HashMap<>();
        for (Map.Entry<String, Long> entry : playtimePerWorld.entrySet()) {
            String worldName = entry.getKey();
            long playtime = entry.getValue();

            if (!aliases.containsKey(worldName)) {
                aliases.put(worldName, worldName);
                aliasSettings.addWorld(worldName);
            }

            String alias = aliases.get(worldName);

            playtimePerAlias.put(alias, playtimePerAlias.getOrDefault(alias, 0L) + playtime);
        }
        return playtimePerAlias;
    }

    @Override
    public String toHighChartsDrilldown() {
        StringBuilder drilldownBuilder = new StringBuilder();
        int i = 0;

        Map<String, GMTimes> gmTimesMap = worldTimes.getWorldTimes();
        if (gmTimesMap.isEmpty()) {
            return "[]";
        }
        Map<String, GMTimes> gmTimesAliasMap = transformToGMAliases(gmTimesMap);

        int size = gmTimesMap.size();
        drilldownBuilder.append("[");
        for (Map.Entry<String, GMTimes> worldAlias : gmTimesAliasMap.entrySet()) {
            drilldownBuilder.append("{name:'").append(worldAlias.getKey())
                    .append("', id:'").append(worldAlias.getKey())
                    .append("',colors: gmPieColors,");
            drilldownBuilder.append("data: [");

            appendGMTimesForWorld(drilldownBuilder, worldAlias);

            if (i < size - 1) {
                drilldownBuilder.append(",");
            }
            i++;
        }
        drilldownBuilder.append("]");
        return drilldownBuilder.toString();
    }

    private Map<String, GMTimes> transformToGMAliases(Map<String, GMTimes> gmTimesMap) {
        // TODO Optimization is possible (WorldAliasSettings)
        WorldAliasSettings aliasSettings = new WorldAliasSettings();
        Map<String, String> aliases = aliasSettings.getAliases();

        Map<String, GMTimes> gmTimesPerAlias = new HashMap<>();

        String[] gms = GMTimes.getGMKeyArray();

        for (Map.Entry<String, GMTimes> entry : gmTimesMap.entrySet()) {
            String worldName = entry.getKey();
            GMTimes gmTimes = entry.getValue();

            if (!aliases.containsKey(worldName)) {
                aliases.put(worldName, worldName);
                aliasSettings.addWorld(worldName);
            }

            String alias = aliases.get(worldName);

            GMTimes aliasGMTimes = gmTimesPerAlias.getOrDefault(alias, new GMTimes());
            for (String gm : gms) {
                aliasGMTimes.addTime(gm, gmTimes.getTime(gm));
            }
            gmTimesPerAlias.put(alias, aliasGMTimes);
        }
        return gmTimesPerAlias;
    }

    private void appendGMTimesForWorld(StringBuilder drilldownBuilder, Map.Entry<String, GMTimes> world) {
        Map<String, Long> gmTimes = world.getValue().getTimes();
        int smallSize = gmTimes.size();
        int j = 0;
        for (Map.Entry<String, Long> entry : gmTimes.entrySet()) {
            Long time = entry.getValue();
            drilldownBuilder.append("['")
                    .append(entry.getKey())
                    .append("',")
                    .append(time)
                    .append("]");

            if (j < smallSize - 1) {
                drilldownBuilder.append(",");
            }
            j++;
        }
        drilldownBuilder.append("]}");
    }
}
