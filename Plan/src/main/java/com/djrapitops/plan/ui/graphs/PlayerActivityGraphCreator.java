package main.java.com.djrapitops.plan.ui.graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.FormatUtils;

/**
 *
 * @author Rsl1122
 */
public class PlayerActivityGraphCreator {

    /**
     *
     * @param sessionData
     * @param scale
     * @param maxPlayers
     * @return
     */
    public static String[] generateDataArray(List<SessionData> sessionData, long scale, int maxPlayers) {
        Benchmark.start("Generate Player Activity Graph " + sessionData.size() + " " + scale + " |");
        long now = new Date().toInstant().getEpochSecond() * (long) 1000;
        long nowMinusScale = now - scale;
        List<List<Long>> s = filterAndTransformSessions(sessionData, nowMinusScale);
        List<Long> sessionStarts = s.get(0);
        List<Long> sessionEnds = s.get(1);

        Benchmark.start("Player Activity Graph Before Addition");
        int amount = (int) sessionStarts.stream().filter(start -> start < nowMinusScale).count();
        for (int i = amount; i > 0; i--) {
            sessionStarts.add(nowMinusScale);
        }
        Benchmark.stop("Player Activity Graph Before Addition");
        Benchmark.start("Player Activity Graph Amount Calculation");
        
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
        Benchmark.stop("Player Activity Graph Amount Calculation");
        playersOnline.add(0L);
        playersOnline.add((long) maxPlayers);
        Benchmark.stop("Generate Player Activity Graph " + sessionData.size() + " " + scale + " |");
        return new String[]{playersOnline.toString(), labels.toString()};
    }

    private static Map<Long, Integer> transformIntoChangeMap(List<Long> sessionStarts, List<Long> sessionEnds) {
        Benchmark.start("Player Activity Graph Calc. Change");
        Map<Long, Integer> starts = sessionStarts.stream().distinct().collect(Collectors.toMap(Function.identity(), start -> Collections.frequency(sessionStarts, start)));
        Map<Long, Integer> ends = sessionEnds.stream().distinct().collect(Collectors.toMap(Function.identity(), end -> Collections.frequency(sessionEnds, end)));
        Set<Long> keys = new HashSet<>(starts.keySet());
        keys.addAll(ends.keySet());
        Map<Long, Integer> change = new HashMap<>();
        keys.stream().forEach((key) -> {
            int value = 0;
            if (starts.containsKey(key)) {
                value += starts.get(key);
            }
            if (ends.containsKey(key)) {
                value -= ends.get(key);
            }
            change.put(key, value);
        });
        Benchmark.stop("Player Activity Graph Calc. Change");
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
        Benchmark.start("Player Activity Graph Transform " + sessionData.size() + " " + nowMinusScale);
        List<Long[]> values = sessionData.parallelStream()
                .filter(session -> (session != null))
                .filter(session -> session.isValid())
                .filter((session) -> (session.getSessionStart() >= nowMinusScale || session.getSessionEnd() >= nowMinusScale))
                .map(session -> new Long[]{session.getSessionStart(), session.getSessionEnd()})
                .collect(Collectors.toList());
        List<Long> sessionStarts = new ArrayList<>();
        List<Long> sessionEnds = new ArrayList<>();
        for (Long[] startAndEnd : values) {
            sessionStarts.add(getSecond(startAndEnd[0]));
            sessionEnds.add(getSecond(startAndEnd[1]));
        }
        List<List<Long>> r = new ArrayList<>();
        r.add(sessionStarts);
        r.add(sessionEnds);
        Benchmark.stop("Player Activity Graph Transform " + sessionData.size() + " " + nowMinusScale);
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
