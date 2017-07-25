package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class PlayerActivityGraphCreator {

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
        List<List<Long>> s = filterAndTransformSessions(sessionData, nowMinusScale);
        List<Long> sessionStarts = s.get(0);
        List<Long> sessionEnds = s.get(1);

        int amount = (int) sessionStarts.stream().filter(start -> start < nowMinusScale).count();
        for (int i = amount; i > 0; i--) {
            sessionStarts.add(nowMinusScale);
        }

        Map<Long, Integer> changeMap = transformIntoChangeMap(sessionStarts, sessionEnds);
        List<Point> points = getPointsFromChangeMap(changeMap);
        return ScatterGraphCreator.scatterGraph(points, false);
    }

    private static List<Point> getPointsFromChangeMap(Map<Long, Integer> changeMap) {
        List<Point> points = new ArrayList<>();
        int lastIndex = -1;
        for (Long key : changeMap.keySet()) {
            long date = key;
            int change = changeMap.get(key);
            if (change != 0) {
                int previousValue = 0;
                if (lastIndex >= 0) {
                    previousValue = (int) points.get(lastIndex).getY();
                }
                points.add(new Point(date, previousValue+change));
            }
        }
        return points;
    }

    private static Map<Long, Integer> transformIntoChangeMap(List<Long> sessionStarts, List<Long> sessionEnds) {
        Map<Long, Integer> starts = sessionStarts.stream().distinct().collect(Collectors.toMap(Function.identity(), start -> Collections.frequency(sessionStarts, start)));
        Map<Long, Integer> ends = sessionEnds.stream().distinct().collect(Collectors.toMap(Function.identity(), end -> Collections.frequency(sessionEnds, end)));
        Set<Long> keys = new HashSet<>(starts.keySet());
        keys.addAll(ends.keySet());
        Map<Long, Integer> change = new HashMap<>();
        keys.forEach((key) -> {
            int value = 0;
            if (starts.containsKey(key)) {
                value += starts.get(key);
            }
            if (ends.containsKey(key)) {
                value -= ends.get(key);
            }
            change.put(key, value);
        });
        return change;
    }

    /**
     * @param values
     * @param lookFor
     * @return
     */
    public static long getCount(List<Long> values, long lookFor) {
        return Collections.frequency(values, lookFor);
    }

    /**
     * @param sessionData
     * @param nowMinusScale
     * @return
     */
    public static List<List<Long>> filterAndTransformSessions(List<SessionData> sessionData, long nowMinusScale) {
        List<Long[]> values = sessionData.parallelStream()
                .filter(session -> (session != null))
                .filter(session -> session.isValid() || session.getSessionEnd() == -1)
                .filter((session) -> (session.getSessionStart() >= nowMinusScale || session.getSessionEnd() >= nowMinusScale))
                .map(session -> new Long[]{session.getSessionStart(), session.getSessionEnd()})
                .collect(Collectors.toList());
        List<Long> sessionStarts = new ArrayList<>();
        List<Long> sessionEnds = new ArrayList<>();
        for (Long[] startAndEnd : values) {
            sessionStarts.add(getSecond(startAndEnd[0]));
            Long end = startAndEnd[1];
            if (end != -1) {
                sessionEnds.add(getSecond(end));
            }
        }
        List<List<Long>> r = new ArrayList<>();
        r.add(sessionStarts);
        r.add(sessionEnds);
        return r;
    }

    /**
     * @param ms
     * @return
     */
    public static long getSecond(long ms) {
        return ms - (ms % 1000);
    }
}
