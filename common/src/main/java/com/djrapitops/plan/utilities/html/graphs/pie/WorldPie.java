package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import com.djrapitops.plan.utilities.comparators.PieSliceComparator;

import java.util.*;

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
        int colLength = colors.length;

        Map<String, Long> playtimePerAlias = AnalysisUtils.getPlaytimePerAlias(worldTimes);

        List<String> worlds = new ArrayList<>(playtimePerAlias.keySet());
        Collections.sort(worlds);

        List<PieSlice> slices = new ArrayList<>();
        int i = 0;
        for (String alias : worlds) {
            Long value = playtimePerAlias.getOrDefault(alias, 0L);
            if (value != 0L) {
                slices.add(new PieSlice(alias, value, colors[i % colLength], true));
            }
            i++;
        }

        return slices;
    }

    private Map<String, GMTimes> getGMTimesPerAlias() {
        Map<String, String> aliases = WorldAliasSettings.getAliases();

        Map<String, GMTimes> gmTimesPerAlias = new HashMap<>();

        String[] gms = GMTimes.getGMKeyArray();

        for (Map.Entry<String, GMTimes> entry : worldTimes.getWorldTimes().entrySet()) {
            String worldName = entry.getKey();
            GMTimes gmTimes = entry.getValue();

            if (!aliases.containsKey(worldName)) {
                aliases.put(worldName, worldName);
                WorldAliasSettings.addWorld(worldName);
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

    @Override
    public String toHighChartsDrilldown() {
        StringBuilder drilldownBuilder = new StringBuilder();
        int i = 0;

        Map<String, GMTimes> gmTimesAliasMap = getGMTimesPerAlias();
        if (gmTimesAliasMap.isEmpty()) {
            return "[]";
        }
        int size = gmTimesAliasMap.size();

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
