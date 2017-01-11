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

    public static String createChart(HashMap<GameMode, Long> gmTimes) {
        long total = gmTimes.get(GameMode.SURVIVAL) + gmTimes.get(GameMode.CREATIVE)
                + gmTimes.get(GameMode.ADVENTURE) + gmTimes.get(GameMode.SPECTATOR);

        return createChart(gmTimes, total);
    }

    public static String createChart(HashMap<GameMode, Long> gmTimes, long total) {
        long gmZero = gmTimes.get(GameMode.SURVIVAL);
        long gmOne = gmTimes.get(GameMode.CREATIVE);
        long gmTwo = gmTimes.get(GameMode.ADVENTURE);
        long gmThree = gmTimes.get(GameMode.SPECTATOR);
        int zero = (int) (gmZero / total);
        int one = (int) (gmOne / total);
        int two = (int) (gmTwo / total);
        int three = (int) (gmThree / total);

        System.out.println(zero + " " + one + " " + two + " " + three + " " + (zero + one + two + three));

        Slice s1 = Slice.newSlice((zero), Color.newColor("951800"), "Survival", "Survival");

        Slice s2 = Slice.newSlice((one), Color.newColor("01A1DB"), "Creative", "Creative");

        Slice s3 = Slice.newSlice((two), Color.newColor("FFFF33"), "Adventure", "Adventure");

        Slice s4 = Slice.newSlice((three), Color.newColor("228B22"), "Spectator", "Spectator");

        PieChart refChart = GCharts.newPieChart(s1, s2, s3, s4);
        refChart.setSize(500, 150);
        refChart.setThreeD(true);
        String refURL = refChart.toURLString();
        return refURL;
    }

}
