package main.java.com.djrapitops.plan.utilities.analysis.locations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import static org.bukkit.Bukkit.getWorlds;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author Rsl1122
 * @since 3.4.0
 */
public class LocationAnalysis {

    public static void performAnalysis(AnalysisData data, Database db) {
        Benchmark.start("Location Analysis");
        try {
            Map<Integer, List<Location>> playerLocations = db.getLocationsTable().getAllLocations(getWorlds().stream().collect(Collectors.toMap(w -> w.getName(), Function.identity())));
            List<Location> locations = new ArrayList<>();
            for (Integer id : playerLocations.keySet()) {
                locations.addAll(playerLocations.get(id));
            }
            Map<String, Map<Point, Integer>> worldPoints = getWorldPoints(locations);
            for (String world : worldPoints.keySet()) {
                Map<Point, Integer> worldLocs = worldPoints.get(world);
                Set<Point> frequentPoints = getFrequentPoints(worldLocs);
                Log.debug(frequentPoints.toString());
            }
        } catch (Exception ex) {
            Log.toLog("LocationAnalysis.performAnalysis", ex);
        }
        Benchmark.stop("Location Analysis");
    }

    public static Map<Point, Object> cluster(Collection<Point> freqPoints, Collection<Point> allPoints) {
        Benchmark.start("LocAnalysis cluster");
        allPoints.removeAll(freqPoints);
        for (Point point : freqPoints) {
            Set<Point> cluster = allPoints.stream().filter(p -> distance(point, p) < 5).collect(Collectors.toSet());
        }
        Benchmark.stop("LocAnalysis cluster");
        return new HashMap<>();
    }

    public static Set<Point> getFrequentPoints(Map<Point, Integer> points) {
        Benchmark.start("LocAnalysis getFrequentPoints");
        if (points.isEmpty()) {
            return new HashSet<>();
        }
        double averageFreq = MathUtils.averageInt(points.values().stream());
        Set<Point> freqPoints = points.entrySet().stream().filter(e -> e.getValue() > averageFreq).map(e -> e.getKey()).collect(Collectors.toSet());
        Benchmark.stop("LocAnalysis getFrequentPoints");
        return freqPoints;
    }

    public static Map<String, Map<Point, Integer>> getWorldPoints(Collection<Location> locations) {
        Benchmark.start("LocAnalysis getWorldPoints");
        Map<String, Map<Point, Integer>> pointMap = new HashMap<>();
        for (Location location : locations) {
            World world = location.getWorld();
            if (world == null) {
                continue;
            }
            String worldName = world.getName();
            if (!pointMap.containsKey(worldName)) {
                pointMap.put(worldName, new HashMap<>());
            }
            Map<Point, Integer> numOfLocs = pointMap.get(worldName);
            Point point = new Point(location.getBlockX(), location.getBlockZ());
            if (!numOfLocs.containsKey(point)) {
                numOfLocs.put(point, 0);
            }
            numOfLocs.replace(point, numOfLocs.get(point) + 1);
        }
        Benchmark.stop("LocAnalysis getWorldPoints");
        return pointMap;
    }

    public static double distance(Point one, Point two) {
        return Math.hypot(one.getX() - two.getX(), one.getY() - one.getY());
    }

}
