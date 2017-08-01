package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class PlayerActivityGraphCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private PlayerActivityGraphCreator() {
        throw new IllegalStateException("Utility class");
    }

    public static String buildSeriesDataString(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getPlayers()))
                .collect(Collectors.toList());
        return SeriesCreator.seriesGraph(points, true);
    }

    public static String buildSeriesDataStringSessions(Collection<SessionData> sessions) {
        List<Point> points = sessions.stream()
                .map(session -> new Point[]{new Point(session.getSessionStart(), 1), new Point(session.getSessionEnd(), 0)})
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(points, true, false);
    }
}
