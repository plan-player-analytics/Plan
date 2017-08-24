package main.java.com.djrapitops.plan.data.analysis;

import main.java.com.djrapitops.plan.utilities.html.graphs.WorldPieCreator;

import java.util.HashMap;
import java.util.Map;

/**
 * Part responsible for all World Playtime related analysis.
 * <p>
 * World times Pie
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following placeholders after analyzed:
 * ${worldTotal} - Total playtime for all worlds
 * ${worldSeries} - Data for HighCharts
 *
 * @author Rsl1122
 * @since 3.6.0
 */
public class WorldPart extends RawData {

    private final Map<String, Long> worldTimes;

    public WorldPart() {
        worldTimes = new HashMap<>();
    }

    @Override
    protected void analyse() {
//   TODO     WorldTimes t = new WorldTimes(worldTimes);
//        addValue("worldTotal", FormatUtils.formatTimeAmount(t.getTotal()));
        addValue("worldSeries", WorldPieCreator.createSeriesData(worldTimes));
    }

    public void addToWorld(String worldName, long playTime) {
        Long value = worldTimes.getOrDefault(worldName, 0L);
        worldTimes.put(worldName, value + playTime);
    }
}
