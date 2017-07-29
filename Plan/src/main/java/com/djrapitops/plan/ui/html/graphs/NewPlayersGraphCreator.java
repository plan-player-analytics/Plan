package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for creating scatter graph data from Registration epoch dates.
 *
 * @author Rsl1122
 * @since 3.6.0
 */
public class NewPlayersGraphCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private NewPlayersGraphCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a scatter data string from given data.
     *
     * @param registered Registration dates of players
     * @param scale      Scale which the graph should reside within. (Milliseconds)
     * @param now        Current epoch ms.
     * @return Scatter Graph data string for ChartJs
     */
    public static String buildScatterDataString(List<Long> registered, long scale, long now) {
        List<Long> filtered = registered.stream()
                .filter(date -> date >= now - scale).collect(Collectors.toList());
        List<Point> points = filtered.stream()
                .distinct()
                .map(date -> new Point(date, getCount(filtered, date)))
                .collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(points, true);
    }

    /**
     * Counts registration amounts of certain date.
     *
     * @param filtered Filtered registration list (Filtered to scale)
     * @param lookFor  Look for this date
     * @return How many were on the list.
     */
    private static long getCount(List<Long> filtered, long lookFor) {
        return filtered.stream().filter(date -> lookFor == date).count();
    }
}
