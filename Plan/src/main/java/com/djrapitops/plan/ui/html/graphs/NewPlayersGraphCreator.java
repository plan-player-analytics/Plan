package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

public class NewPlayersGraphCreator {

    public static String buildScatterDataString(List<Long> registered, long scale, long now) {
        List<Long> filtered = registered.stream()
                .filter(date -> date >= now - scale).collect(Collectors.toList());
        List<Point> points = filtered.stream()
                .distinct()
                .map(date -> new Point(date, getCount(filtered, date)))
                .collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(points, true);
    }

    private static long getCount(List<Long> filtered, long lookFor) {
        return filtered.stream().filter(date -> lookFor == date).count();
    }
}
