package main.java.com.djrapitops.plan.ui.graphs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.data.SessionData;
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
        long now = new Date().toInstant().getEpochSecond() * (long) 1000;
        long nowMinusScale = now - scale;
        List<List<Long>> s = filterSessions(sessionData, nowMinusScale);
        List<Long> sessionStarts = s.get(0);
        List<Long> sessionEnds = s.get(1);
        List<Long> playersOnline = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (Long start : sessionStarts) {
            if (start < nowMinusScale) {
                sessionStarts.add(nowMinusScale);
            }
        }

        long lastPValue = 0;
        long lastSavedPValue = -1;
        long lastSaveI = 0;
        for (long i = nowMinusScale; i <= now; i += 1000) {
            if (sessionStarts.contains(i)) {
                lastPValue += getCount(sessionStarts, i);
            }
            if (sessionEnds.contains(i)) {
                lastPValue -= getCount(sessionEnds, i);
            }

            if (lastSavedPValue != lastPValue || i - lastSaveI > (scale / (long) 75)) {
                lastSaveI = i;
                labels.add("\"" + FormatUtils.formatTimeStamp(i + "") + "\"");
                lastSavedPValue = lastPValue;
                playersOnline.add(lastPValue);
            }
        }
        playersOnline.add(0L);
        playersOnline.add((long) maxPlayers);
        return new String[]{playersOnline.toString(), labels.toString()};
    }

    public static long getCount(List<Long> values, long lookFor) {
        return values.stream()
                .filter((start) -> (start == lookFor))
                .count();
    }

    public static List<List<Long>> filterSessions(List<SessionData> sessionData, long nowMinusScale) {        
        List<Long[]> values = sessionData.parallelStream()
                .filter(session -> (session != null))
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
        return r;
    }
    
    public static long getSecond(long ms) {
        return ms - (ms % 1000);
    }
}
