package main.java.com.djrapitops.plan.ui.graphs;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.utilities.FormatUtils;
import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.XYLine;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import main.java.com.djrapitops.plan.data.SessionData;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class PlayerActivityGraphCreator {

    /**
     * Creates a new url for a PlayerActivity graph.
     *
     * @param sessionData SessionData of Players in scale.
     * @param scale Long in ms, time the graph will be limited to.
     * @return Url of charts4j image link.
     */
    public static String createChart(List<SessionData> sessionData, long scale) {

        List<Long> sessionStarts = new ArrayList<>();
        List<Long> sessionEnds = new ArrayList<>();
        sessionData.parallelStream().forEach(
                (session) -> {
                    sessionEnds.add(session.getSessionEnd());
                    sessionStarts.add(session.getSessionStart());
                });
        List<Double> xListDate = new ArrayList<>();
        List<Double> pYList = new ArrayList<>();

        List<String> xDateAxisLabels = new ArrayList<>();
        List<Double> xDateAxisLabelsLocations = new ArrayList<>();
        Plan plugin = getPlugin(Plan.class);

        int maxPlayers = plugin.getHandler().getMaxPlayers();
        long now = new Date().toInstant().getEpochSecond() * (long) 1000;
        long nowMinusScale = now - scale;

        int lastPValue = 0;
        int lastSavedPValue = -1;
        long lastSaveI = 0;
        for (long i = nowMinusScale; i <= now; i += 1000) {
            if (sessionStarts.contains(i)) {
                int amount = 0;
                for (long start : sessionStarts) {
                    if (start == i) {
                        amount++;
                    }
                }
                lastPValue += amount;
            }
            if (sessionEnds.contains(i)) {
                int amount = 0;
                for (long end : sessionEnds) {
                    if (end == i) {
                        amount++;
                    }
                }
                lastPValue -= amount;
            }
            Double scaledDateValue = ((i - nowMinusScale) * 1.0 / scale) * 100;
            Double scaledPlayerValue = (lastPValue * 1.0 / maxPlayers) * 100;

            if (lastSavedPValue != lastPValue || i - lastSaveI > (scale / (long) 100)) {
                lastSaveI = i;
                xListDate.add(scaledDateValue);
                pYList.add((lastSavedPValue * 1.0 / maxPlayers) * 100);
                lastSavedPValue = lastPValue;
                xListDate.add(scaledDateValue);
                pYList.add(scaledPlayerValue);
            }
        }

        // Date labels
        for (long j = 0; j <= 8; j++) {
            long scaleAddition = j * (scale / 8);
            xDateAxisLabels.add(FormatUtils.formatTimeStamp("" + (nowMinusScale + scaleAddition)));
            xDateAxisLabelsLocations.add((scaleAddition * 1.0 / scale) * 100);
        }
        // Player labels
        List<String> yAxisLabels = new ArrayList<>();
        for (int k = 0; k <= maxPlayers; k++) {
            if (k % 5 == 0) {
                yAxisLabels.add("" + k);
            }
        }

        AxisLabels xAxisLabels = AxisLabelsFactory.newAxisLabels(xDateAxisLabels, xDateAxisLabelsLocations);
        Data xData = Data.newData(xListDate);
        Data pYData = Data.newData(pYList);

        XYLine playerLine = Plots.newXYLine(xData, pYData, Color.newColor(Phrase.HCOLOR_ACT_ONL + ""), "Players Online");
        LineChart chart = GCharts.newLineChart(playerLine);
        chart.addXAxisLabels(xAxisLabels);
        chart.addTopAxisLabels(AxisLabelsFactory.newAxisLabels("Players", 1));
        chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels(yAxisLabels));
        chart.addRightAxisLabels(AxisLabelsFactory.newAxisLabels("Date", 4));
        chart.setSize(1000, 250);
        return chart.toURLString();
    }

}
