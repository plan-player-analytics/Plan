package com.djrapitops.plan.ui.graphs;

import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.PieChart;
import com.googlecode.charts4j.Slice;
import java.util.HashMap;
import org.bukkit.GameMode;

/**
 *
 * @author Rsl1122
 */
public class GMTimesPieChartCreator {

    public static String createChart(HashMap<GameMode, Long> gmTimes, String uuid) {

        long gmZero = gmTimes.get(GameMode.SURVIVAL);
        long gmOne = gmTimes.get(GameMode.CREATIVE);
        long gmTwo = gmTimes.get(GameMode.ADVENTURE);
        long gmThree = gmTimes.get(GameMode.SPECTATOR);

        long total = gmZero + gmOne + gmTwo + gmThree;

        Slice s1 = Slice.newSlice((int) (gmZero / total), Color.newColor("951800"), "Survival", "Survival");
        Slice s2 = Slice.newSlice((int) (gmOne / total), Color.newColor("01A1DB"), "Creative", "Creative");
        Slice s3 = Slice.newSlice((int) (gmThree / total), Color.newColor("FFFF33"), "Adventure", "Adventure");
        Slice s4 = Slice.newSlice((int) (gmTwo / total), Color.newColor("228B22"), "Spectator", "Spectator");

        PieChart refChart = GCharts.newPieChart(s1, s2, s3, s4);
        refChart.setSize(500, 150);
        refChart.setThreeD(true);
        String refURL = refChart.toURLString();
        return refURL;
    }

}
