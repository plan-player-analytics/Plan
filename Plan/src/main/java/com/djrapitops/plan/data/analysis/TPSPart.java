package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.api.TimeAmount;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.ui.html.graphs.CPUGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.RamGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.TPSGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.WorldLoadGraphCreator;
import main.java.com.djrapitops.plan.ui.theme.Colors;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.util.List;

/**
 * Part responsible for all TPS related analysis.
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following placeholders after being analyzed:
 * ${tpsSeries} - HighCharts data
 * ${cpuSeries} - HighCharts data
 * ${ramSeries} - HighCharts data
 * ${entitySeries} - HighCharts data
 * ${chunkSeries} - HighCharts data
 * <p>
 * ${tpsAverageDay} - (Number)
 * ${tpsAverageWeek} - (Number)
 * ${cpuAverageDay} - (Number)%
 * ${cpuAverageWeek} - (Number)%
 * ${ramAverageDay} - (Number) MB
 * ${ramAverageWeek} - (Number) MB
 * ${entityAverageDay} - (Number)
 * ${entityAverageWeek} - (Number)
 * ${chunkAverageDay} - (Number)
 * ${chunkAverageWeek} - (Number)
 * <p>
 * ${tpsMedium} - (Number) Color Threshold for Medium TPS
 * ${tpsHigh} - (Number) Color Threshold for High TPS
 * ${tpsLowColor} - Color of Low TPS
 * ${tpsMediumColor} - Color of Low TPS
 * ${tpsHighColor} - Color of Low TPS
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class TPSPart extends RawData {

    private final List<TPS> tpsData;

    public TPSPart(List<TPS> tpsData) {
        this.tpsData = tpsData;
    }

    @Override
    public void analyse() {
        long now = MiscUtils.getTime();
        List<TPS> week = TPSGraphCreator.filterTPS(tpsData, now - TimeAmount.WEEK.ms());
        List<TPS> day = TPSGraphCreator.filterTPS(tpsData, now - TimeAmount.DAY.ms());

        addValue("tpsHighColor", Colors.TPS_HIGH.getColor());
        addValue("tpsMediumColor", Colors.TPS_MED.getColor());
        addValue("tpsLowColor", Colors.TPS_LOW.getColor());
        addValue("tpsMedium", Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber());
        addValue("tpsHigh", Settings.THEME_GRAPH_TPS_THRESHOLD_HIGH.getNumber());

        addValue("tpsSeries", TPSGraphCreator.buildSeriesDataString(tpsData));
        addValue("cpuSeries", CPUGraphCreator.buildSeriesDataString(tpsData));
        addValue("ramSeries", RamGraphCreator.buildSeriesDataString(tpsData));
        addValue("entitySeries", WorldLoadGraphCreator.buildSeriesDataStringEntities(tpsData));
        addValue("chunkSeries", WorldLoadGraphCreator.buildSeriesDataStringChunks(tpsData));

        double averageTPSWeek = MathUtils.averageDouble(week.stream().map(TPS::getTicksPerSecond));
        double averageTPSDay = MathUtils.averageDouble(day.stream().map(TPS::getTicksPerSecond));

        double averageCPUWeek = MathUtils.averageDouble(week.stream().map(TPS::getCPUUsage).filter(i -> i != 0));
        double averageCPUDay = MathUtils.averageDouble(day.stream().map(TPS::getCPUUsage).filter(i -> i != 0));

        long averageUsedMemoryWeek = MathUtils.averageLong(week.stream().map(TPS::getUsedMemory).filter(i -> i != 0));
        long averageUsedMemoryDay = MathUtils.averageLong(day.stream().map(TPS::getUsedMemory).filter(i -> i != 0));

        double averageEntityCountWeek = MathUtils.averageInt(week.stream().map(TPS::getEntityCount).filter(i -> i != 0));
        double averageEntityCountDay = MathUtils.averageInt(day.stream().map(TPS::getEntityCount).filter(i -> i != 0));

        double averageChunksLoadedWeek = MathUtils.averageInt(week.stream().map(TPS::getChunksLoaded).filter(i -> i != 0));
        double averageChunksLoadedDay = MathUtils.averageInt(day.stream().map(TPS::getChunksLoaded).filter(i -> i != 0));

        addValue("tpsAverageWeek", FormatUtils.cutDecimals(averageTPSWeek));
        addValue("tpsAverageDay", FormatUtils.cutDecimals(averageTPSDay));

        addValue("cpuAverageWeek", averageCPUWeek >= 0 ? FormatUtils.cutDecimals(averageCPUWeek) + "%" : "Unavailable");
        addValue("cpuAverageDay", averageCPUDay >= 0 ? FormatUtils.cutDecimals(averageCPUDay) + "%" : "Unavailable");

        addValue("ramAverageWeek", FormatUtils.cutDecimals(averageUsedMemoryWeek));
        addValue("ramAverageDay", FormatUtils.cutDecimals(averageUsedMemoryDay));

        addValue("entityAverageWeek", FormatUtils.cutDecimals(averageEntityCountWeek));
        addValue("entityAverageDay", FormatUtils.cutDecimals(averageEntityCountDay));

        addValue("chunkAverageWeek", FormatUtils.cutDecimals(averageChunksLoadedWeek));
        addValue("chunkAverageDay", FormatUtils.cutDecimals(averageChunksLoadedDay));
    }

    public List<TPS> getTpsData() {
        return tpsData;
    }
}
