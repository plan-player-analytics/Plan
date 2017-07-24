package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Rsl1122
 */
public class PlayerActivityGraphCreator {

    @Deprecated
    public static String[] generateArray(List<TPS> tpsData, long scale) {
        long now = MiscUtils.getTime();
        List<TPS> filtered = tpsData.stream().filter(tps -> tps.getDate() >= now - scale).collect(Collectors.toList());
        String players = filtered.stream().map(TPS::getPlayers).collect(Collectors.toList()).toString();
        String dates = filtered.stream().map(TPS::getDate).collect(Collectors.toList()).toString();
        return new String[]{players, dates};
    }

    public static String buildScatterDataString(List<TPS> tpsData, long scale) {
        long now = MiscUtils.getTime();
        List<Point> points = tpsData.stream().filter(tps -> tps.getDate() >= now - scale).map(tps -> new Point(tps.getDate(), tps.getPlayers())).collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(points, true);
    }

    /**
     *
     * @param sessionData
     * @param scale
     * @return
     */
    public static String[] generateDataArray(List<SessionData> sessionData, long scale) {
        long now = MiscUtils.getTime();
        long nowMinusScale = now - scale;
        List<List<Long>> s = filterAndTransformSessions(sessionData, nowMinusScale);
        List<Long> sessionStarts = s.get(0);
        List<Long> sessionEnds = s.get(1);

        int amount = (int) sessionStarts.stream().filter(start -> start < nowMinusScale).count();
        for (int i = amount; i > 0; i--) {
            sessionStarts.add(nowMinusScale);
        }

        Map<Long, Integer> change = transformIntoChangeMap(sessionStarts, sessionEnds);

        long lastPValue = 0;
        long lastSavedPValue = -1;
        long lastSaveIndex = 0;
        List<Long> playersOnline = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (long i = nowMinusScale / 1000; i <= now / 1000; i += 1) {
            long index = i * 1000;
            boolean contains = change.containsKey(index);
            boolean isBelowMinimumScaleThreshold = index - lastSaveIndex > (scale / (long) 75);
            if (!(contains || isBelowMinimumScaleThreshold)) {
                continue;
            }
            if (contains) {
                lastPValue += change.get(index);
            }
            if (isBelowMinimumScaleThreshold || lastSavedPValue != lastPValue) {
                lastSaveIndex = index;
                labels.add("\"" + FormatUtils.formatTimeStamp(index) + "\"");
                lastSavedPValue = lastPValue;
                playersOnline.add(lastPValue);
            }
        }
        if (Settings.ANALYSIS_REMOVE_OUTLIERS.isTrue()) {
            long average = MathUtils.averageLong(playersOnline.stream());
            double standardDeviation = getStandardDeviation(playersOnline, average);
            if (standardDeviation > 3.5) {
                for (int i = 0; i < playersOnline.size(); i++) {
                    long value = playersOnline.get(i);
                    if (value - average > 3 * standardDeviation) {
                        playersOnline.set(i, (long) Plan.getInstance().getVariable().getMaxPlayers() + 10);
                    }
                }
            }
        }
        return new String[]{playersOnline.toString(), labels.toString()};
    }

    private static double getStandardDeviation(List<Long> players, long avg) {
        List<Double> valueMinusAvg = players.stream()
                .map(p -> Math.pow(Math.abs(p - avg), 2))
                .collect(Collectors.toList());
        int size = valueMinusAvg.size();
        double sum = MathUtils.sumDouble(valueMinusAvg.stream().map(p -> (Serializable) p));
        return Math.sqrt(sum / size);
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
     *
     * @param values
     * @param lookFor
     * @return
     */
    public static long getCount(List<Long> values, long lookFor) {
        return Collections.frequency(values, lookFor);
//        values.stream()
//                .filter((start) -> (start == lookFor))
//                .count();
    }

    /**
     *
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
     *
     * @param ms
     * @return
     */
    public static long getSecond(long ms) {
        return ms - (ms % 1000);
    }
}
