package main.java.com.djrapitops.plan.ui.graphs;

import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.PieChart;
import com.googlecode.charts4j.Slice;

/**
 *
 * @author Rsl1122
 */
public class ActivityPieChartCreator {

    public static String createChart(int totalBanned, int active, int inactive) {

        int total = totalBanned + active + inactive;

        int banPerc = (int) ((totalBanned / total) * 100);
        int inacPerc = (int) ((inactive / total) * 100);
        int actPerc = (int) ((active / total) * 100);
        while (banPerc + inacPerc + actPerc < 100) {
            actPerc++;
        }
        while (banPerc + inacPerc + actPerc > 100) {
            actPerc--;
        }

        Slice s1 = Slice.newSlice((int) (banPerc), Color.newColor("951800"), "Banned", "Banned");
        Slice s3 = Slice.newSlice((int) (inacPerc), Color.newColor("A9A9A9"), "Inactive", "Inactive");
        Slice s4 = Slice.newSlice((int) (actPerc), Color.newColor("228B22"), "Active", "Active");

        PieChart refChart = GCharts.newPieChart(s4, s3, s1);
        refChart.setSize(500, 150);
        refChart.setThreeD(true);
        String refURL = refChart.toURLString();
        return refURL;
    }

}
