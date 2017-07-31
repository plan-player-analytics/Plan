package main.java.com.djrapitops.plan.data.analysis;

import main.java.com.djrapitops.plan.ui.html.graphs.WorldPieCreator;
import main.java.com.djrapitops.plan.utilities.FormatUtils;

import java.util.HashMap;
import java.util.Map;

public class WorldPart extends RawData {

    private final Map<String, Long> worldTimes;

    public WorldPart() {
        worldTimes = new HashMap<>();
    }

    @Override
    protected void analyse() {
        addValue("worldtotal", FormatUtils.formatTimeAmount(worldTimes.values().stream().mapToLong(Long::longValue).sum()));
        addValue("worldseries", WorldPieCreator.createSeriesData(worldTimes));
    }

    public void addToWorld(String worldName, long playTime) {
        Long value = worldTimes.computeIfAbsent(worldName, ifNotFound -> 0L);
        worldTimes.put(worldName, value + playTime);
    }

}
