package main.java.com.djrapitops.plan.ui.graphs;

import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.PieChart;
import com.googlecode.charts4j.Slice;
import main.java.com.djrapitops.plan.Phrase;

/**
 *
 * @author Rsl1122
 */
public class ActivityPieChartCreator {

    /**
     * Creates a image link to Activity Chart.
     *
     * @param totalBanned Number of Banned Players
     * @param active Number of Active Players
     * @param inactive Number of Inactive Players
     * @param joinleaver Number of players who have joined only once
     * @return Url to Image link.
     */
    public static String createChart(int totalBanned, int active, int inactive, int joinleaver) {

        int total = totalBanned + active + inactive + joinleaver;

        int banPerc = (int) ((totalBanned * 1.0 / total) * 100);
        int inacPerc = (int) ((inactive * 1.0 / total) * 100);
        int actPerc = (int) ((active * 1.0 / total) * 100);
        int joinlPerc = (int) ((joinleaver * 1.0 / total) * 100);
        while (banPerc + inacPerc + actPerc + joinlPerc < 100) {
            actPerc++;
        }
        while (banPerc + inacPerc + actPerc + joinlPerc > 100) {
            actPerc--;
        }

        Slice bannedSlice = Slice.newSlice((int) (banPerc), Color.newColor(Phrase.HCOLOR_ACTP_BAN + ""), "Banned", "Banned");
        Slice joinLeaverSlice = Slice.newSlice((int) (joinlPerc), Color.newColor(Phrase.HCOLOR_ACTP_JON + ""), "Unknown", "Unknown");
        Slice inactiveSlice = Slice.newSlice((int) (inacPerc), Color.newColor(Phrase.HCOLOR_ACTP_INA + ""), "Inactive", "Inactive");
        Slice activeSlice = Slice.newSlice((int) (actPerc), Color.newColor(Phrase.HCOLOR_ACTP_ACT + ""), "Active", "Active");

        PieChart refChart = GCharts.newPieChart(activeSlice, bannedSlice, inactiveSlice, joinLeaverSlice);
        refChart.setSize(400, 150);
        refChart.setThreeD(true);
        String refURL = refChart.toURLString();
        return refURL;
    }
}
