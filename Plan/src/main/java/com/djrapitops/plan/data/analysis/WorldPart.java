package main.java.com.djrapitops.plan.data.analysis;

import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.html.graphs.WorldPieCreator;

import java.util.HashMap;

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

    private WorldTimes worldTimes;

    public WorldPart() {
        worldTimes = new WorldTimes(new HashMap<>());
    }

    @Override
    protected void analyse() {
        addValue("worldTotal", FormatUtils.formatTimeAmount(worldTimes.getTotal()));
        String[] seriesData = WorldPieCreator.createSeriesData(worldTimes);
        addValue("worldSeries", seriesData[0]);
        addValue("gmSeries", seriesData[1]);
        addValue("worldPieColors", Settings.THEME_GRAPH_WORLD_PIE.toString());
    }

    public WorldTimes getWorldTimes() {
        return worldTimes;
    }

    public void setWorldTimes(WorldTimes worldTimes) {
        this.worldTimes = worldTimes;
    }
}
