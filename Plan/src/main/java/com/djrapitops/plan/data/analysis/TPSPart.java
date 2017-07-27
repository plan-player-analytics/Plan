package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.api.TimeAmount;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.ui.html.graphs.CPUGraphCreator;
import main.java.com.djrapitops.plan.ui.html.graphs.TPSGraphCreator;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.util.List;

/**
 * Part responsible for all TPS related analysis.
 * <p>
 * Ticks Per Second Graphs
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following place-holders: tpsscatterday, tpsscatterweek, cpuscatterday, cpuscatterweek, averagetps(-week),
 * averagetpsday, averagecpuday, averagecpuweek, averagememoryday, averagememoryweek, averageentitiesday, averageentitiesweek,
 * averagechunksday, averagechunkweek, ramscatterday, ramscatterweek
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class TPSPart extends RawData<TPSPart> {

    private final List<TPS> tpsData;

    public TPSPart(List<TPS> tpsData) {
        this.tpsData = tpsData;
    }

    @Override
    public void analyse() {
        long now = MiscUtils.getTime();
        List<TPS> week = TPSGraphCreator.filterTPS(tpsData, now - TimeAmount.WEEK.ms());
        List<TPS> day = TPSGraphCreator.filterTPS(tpsData, now - TimeAmount.DAY.ms());

        String tpsScatterDay = TPSGraphCreator.buildScatterDataStringTPS(day, TimeAmount.DAY.ms());
        String tpsScatterWeek = TPSGraphCreator.buildScatterDataStringTPS(week, TimeAmount.WEEK.ms());
        String cpuScatterDay = CPUGraphCreator.buildScatterDataString(day, TimeAmount.DAY.ms());
        String cpuScatterWeek = CPUGraphCreator.buildScatterDataString(week, TimeAmount.WEEK.ms());
        String ramScatterDay = CPUGraphCreator.buildScatterDataString(day, TimeAmount.DAY.ms());
        String ramScatterWeek = CPUGraphCreator.buildScatterDataString(week, TimeAmount.WEEK.ms());

        addValue("tpsscatterday", tpsScatterDay);
        addValue("tpsscatterweek", tpsScatterWeek);

        addValue("cpuscatterday", cpuScatterDay);
        addValue("cpuscatterweek", cpuScatterWeek);

        addValue("ramscatterday", ramScatterDay);
        addValue("ramscatterweek", ramScatterWeek);

        Runtime runtime = Runtime.getRuntime();
        addValue("maxram", (runtime.maxMemory() / (1024L * 1024L)));

        double averageTPSWeek = MathUtils.averageDouble(week.stream().map(TPS::getTps));
        double averageTPSDay = MathUtils.averageDouble(day.stream().map(TPS::getTps));

        double averageCPUWeek = MathUtils.averageDouble(week.stream().map(TPS::getCPUUsage));
        double averageCPUDay = MathUtils.averageDouble(day.stream().map(TPS::getCPUUsage));

        long averageUsedMemoryWeek = MathUtils.averageLong(week.stream().map(TPS::getUsedMemory));
        long averageUsedMemoryDay = MathUtils.averageLong(day.stream().map(TPS::getUsedMemory));

        double averageEntityCountWeek = MathUtils.averageInt(week.stream().map(TPS::getEntityCount));
        double averageEntityCountDay = MathUtils.averageInt(day.stream().map(TPS::getEntityCount));

        double averageChunksLoadedWeek = MathUtils.averageInt(week.stream().map(TPS::getChunksLoaded));
        double averageChunksLoadedDay = MathUtils.averageInt(day.stream().map(TPS::getChunksLoaded));

        addValue("averagetps", FormatUtils.cutDecimals(averageTPSWeek)); //Staying for backwards compatibility
        addValue("averagetpsweek", FormatUtils.cutDecimals(averageTPSWeek));
        addValue("averagetpsday", FormatUtils.cutDecimals(averageTPSDay));

        addValue("averagecpuweek", FormatUtils.cutDecimals(averageCPUWeek));
        addValue("averagecpuday", FormatUtils.cutDecimals(averageCPUDay));

        addValue("averagememoryweek", FormatUtils.cutDecimals(averageUsedMemoryWeek));
        addValue("averagememoryday", FormatUtils.cutDecimals(averageUsedMemoryDay));

        addValue("averageentitiesweek", FormatUtils.cutDecimals(averageEntityCountWeek));
        addValue("averageentitiesday", FormatUtils.cutDecimals(averageEntityCountDay));

        addValue("averagechunksweek", FormatUtils.cutDecimals(averageChunksLoadedWeek));
        addValue("averagechunksday", FormatUtils.cutDecimals(averageChunksLoadedDay));
    }

    public List<TPS> getTpsData() {
        return tpsData;
    }
}
