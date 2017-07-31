package main.java.com.djrapitops.plan.data.analysis;

import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.ui.html.graphs.WorldPieCreator;
import main.java.com.djrapitops.plan.utilities.FormatUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Part responsible for all World Playtime related analysis.
 * <p>
 * World times Pie
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following place-holders: worldtotal, worldseries
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
        WorldTimes t = new WorldTimes(worldTimes);
        addValue("worldtotal", FormatUtils.formatTimeAmount(t.getTotal()));
        addValue("worldseries", WorldPieCreator.createSeriesData(worldTimes));
    }

    public void addToWorld(String worldName, long playTime) {
        Long value = worldTimes.computeIfAbsent(worldName, ifNotFound -> 0L);
        worldTimes.put(worldName, value + playTime);
    }
}
