package com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.RetentionData;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that contains various methods that are used in analysis.
 *
 * @author Rsl1122
 */
public class AnalysisUtils {

    private AnalysisUtils() {
        /* static method class.*/
    }

    /**
     * Transforms the session start list into a list of int arrays.
     * <p>
     * First number signifies the Day of Week. (0 = Monday, 6 = Sunday)
     * Second number signifies the Hour of Day. (0 = 0 AM, 23 = 11 PM)
     *
     * @param sessionStarts List of Session start Epoch ms.
     * @return list of int arrays.
     */
    public static List<int[]> getDaysAndHours(List<Long> sessionStarts) {
        return sessionStarts.stream().map((Long start) -> {
            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(start);
            int hourOfDay = day.get(Calendar.HOUR_OF_DAY); // 0 AM is 0
            int dayOfWeek = day.get(Calendar.DAY_OF_WEEK) - 2; // Monday is 0, Sunday is -1
            if (hourOfDay == 24) { // If hour is 24 (Should be impossible but.)
                hourOfDay = 0;
                dayOfWeek += 1;
            }
            if (dayOfWeek > 6) { // If Hour added a day on Sunday, move to Monday
                dayOfWeek = 0;
            }
            if (dayOfWeek < 0) { // Move Sunday to 6
                dayOfWeek = 6;
            }
            return new int[]{dayOfWeek, hourOfDay};
        }).collect(Collectors.toList());
    }

    public static double getAveragePerDay(long after, long before, long total) {
        return (double) total / getNumberOfDaysBetween(after, before);
    }

    public static long getNumberOfDaysBetween(long start, long end) {
        long value = 0;
        long test = start;
        long day = TimeAmount.DAY.ms();
        while (test < end) {
            test += day;
            value++;
        }
        return value == 0 ? 1 : value;
    }

    public static Map<UUID, List<Session>> sortSessionsByUser(Map<UUID, Map<UUID, List<Session>>> allSessions) {
        Map<UUID, List<Session>> userSessions = new HashMap<>();

        for (Map<UUID, List<Session>> sessions : allSessions.values()) {
            for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
                UUID uuid = entry.getKey();
                List<Session> list = userSessions.getOrDefault(uuid, new ArrayList<>());
                list.addAll(entry.getValue());
                userSessions.put(uuid, list);
            }
        }

        return userSessions;
    }

    public static Map<String, Long> getPlaytimePerAlias(WorldTimes worldTimes) {
        // WorldTimes Map<String, GMTimes>
        Map<String, Long> playtimePerWorld = worldTimes.getWorldTimes()
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getTotal() // GMTimes.getTotal
                ));

        Map<String, String> aliases = WorldAliasSettings.getAliases();

        Map<String, Long> playtimePerAlias = new HashMap<>();
        for (Map.Entry<String, Long> entry : playtimePerWorld.entrySet()) {
            String worldName = entry.getKey();
            long playtime = entry.getValue();

            if (!aliases.containsKey(worldName)) {
                aliases.put(worldName, worldName);
                WorldAliasSettings.addWorld(worldName);
            }

            String alias = aliases.get(worldName);

            playtimePerAlias.put(alias, playtimePerAlias.getOrDefault(alias, 0L) + playtime);
        }
        return playtimePerAlias;
    }

    public static RetentionData average(Collection<RetentionData> stuck) {
        int size = stuck.size();

        double totalIndex = 0.0;
        double totalPlayersOnline = 0.0;

        for (RetentionData retentionData : stuck) {
            totalIndex += retentionData.getActivityIndex();
            totalPlayersOnline += retentionData.getOnlineOnJoin();
        }

        double averageIndex = totalIndex / (double) size;
        double averagePlayersOnline = totalPlayersOnline / (double) size;

        return new RetentionData(averageIndex, averagePlayersOnline);
    }

    public static String getLongestWorldPlayed(Session session) {
        Map<String, String> aliases = WorldAliasSettings.getAliases();
        if (!session.supports(SessionKeys.WORLD_TIMES)) {
            return "No World Time Data";
        }
        if (!session.supports(SessionKeys.END)) {
            return "Current: " + aliases.get(session.getUnsafe(SessionKeys.WORLD_TIMES).getCurrentWorld());
        }

        WorldTimes worldTimes = session.getUnsafe(SessionKeys.WORLD_TIMES);
        Map<String, Long> playtimePerAlias = getPlaytimePerAlias(worldTimes);
        long total = worldTimes.getTotal();

        long longest = 0;
        String theWorld = "-";
        for (Map.Entry<String, Long> entry : playtimePerAlias.entrySet()) {
            String world = entry.getKey();
            long time = entry.getValue();
            if (time > longest) {
                longest = time;
                theWorld = world;
            }
        }

        double percentage = longest * 100.0 / total;

        return theWorld + " (" + FormatUtils.cutDecimals(percentage) + "%)";
    }
}
