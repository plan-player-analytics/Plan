package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

    public static String buildScatterDataString(List<TPS> tpsData, long scale) {
        long now = MiscUtils.getTime();
        List<Point> points = tpsData.stream()
                .filter(tps -> tps.getDate() >= now - scale)
                .map(tps -> new Point(tps.getDate(), tps.getPlayers()))
                .collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(points, true);
    }

    public static String buildScatterDataStringSessions(List<SessionData> sessionData, long scale) {
        long now = MiscUtils.getTime();
        long nowMinusScale = now - scale;
        List<SessionData> filtered = filterSessions(sessionData, nowMinusScale);

        List<Point> points = filtered.stream()
                .map(session -> new Point[]{new Point(session.getSessionStart(), 1), new Point(session.getSessionEnd(), 0)})
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        Log.debug(points.stream().map(Point::getY).collect(Collectors.toList()).toString());
        return ScatterGraphCreator.scatterGraph(points, true, false);
    }

    private static List<SessionData> filterSessions(List<SessionData> sessions, long nowMinusScale) {
        return sessions.parallelStream()
                .filter(Objects::nonNull)
                .filter(session -> session.isValid() || session.getSessionEnd() == -1)
                .filter(session -> session.getSessionStart() >= nowMinusScale || session.getSessionEnd() >= nowMinusScale)
                .distinct()
                .collect(Collectors.toList());
    }
}
