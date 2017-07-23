package main.java.com.djrapitops.plan.ui.html.graphs;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import main.java.com.djrapitops.plan.utilities.comparators.TPSComparator;

/**
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class TPSGraphCreator {
    
    public static String[] generateDataArray(List<TPS> tpsData, long scale) {
        Benchmark.start("TPSGraph: generate array");
        long now = MiscUtils.getTime();
        List<TPS> filtered = filterTPS(tpsData, now - scale);
        Log.debug("TPSGraph, filtered: " + filtered.size());
        filtered.sort(new TPSComparator());
        List<Long> dates = filtered.stream().map(t -> t.getDate()).collect(Collectors.toList());
        List<Double> tps = filtered.stream().map(t -> t.getTps()).collect(Collectors.toList());
        List<Integer> players = filtered.stream().map(t -> t.getPlayers()).collect(Collectors.toList());
        Benchmark.stop("TPSGraph: generate array");
        return new String[]{dates.toString(), tps.toString(), players.toString()};
    }
    
    public static String buildScatterDataStringTPS(List<TPS> tpsData, long scale) {
        long now = MiscUtils.getTime();
        List<Point> points = tpsData.stream().filter(tps -> tps.getDate() >= now - scale).map(tps -> new Point(tps.getDate(), tps.getTps())).collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(points, true);
    }
    
    public static List<TPS> filterTPS(List<TPS> tpsData, long nowMinusScale) {
        return tpsData.stream()
                .filter(t -> t != null)
                .filter(t -> t.getDate() >= nowMinusScale)
                .collect(Collectors.toList());
    }
}
