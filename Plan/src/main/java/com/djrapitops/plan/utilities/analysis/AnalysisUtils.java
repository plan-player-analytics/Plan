package com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.StickyData;
import com.djrapitops.plan.data.element.ActivityIndex;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class AnalysisUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private AnalysisUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static long getNewPlayers(List<Long> registered, long scale, long now) {
        long newPlayers = 0;
        if (!registered.isEmpty()) {
            newPlayers = registered.stream()
                    .filter(Objects::nonNull)
                    .filter(reg -> reg > now - scale)
                    .count();
        }
        // Filters out register dates before scale
        return newPlayers;
    }

    public static int getUniqueJoinsPerDay(Map<UUID, List<Session>> sessions, long after) {
        Map<Integer, Set<UUID>> uniqueJoins = new HashMap<>();

        sessions.forEach((uuid, s) -> {
            for (Session session : s) {
                if (session.getSessionStart() < after) {
                    continue;
                }

                int day = getDayOfYear(session);

                uniqueJoins.computeIfAbsent(day, computedDay -> new HashSet<>());
                uniqueJoins.get(day).add(uuid);
            }
        });

        int total = MathUtils.sumInt(uniqueJoins.values().stream().map(Set::size));
        int numberOfDays = uniqueJoins.size();

        if (numberOfDays == 0) {
            return 0;
        }

        return total / numberOfDays;
    }

    public static long getNewUsersPerDay(List<Long> registers, long after, long total) {
        Set<Integer> days = new HashSet<>();
        for (Long date : registers) {
            if (date < after) {
                continue;
            }
            int day = getDayOfYear(date);
            days.add(day);
        }
        int numberOfDays = days.size();

        if (numberOfDays == 0) {
            return 0;
        }
        return total / numberOfDays;
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
            if (hourOfDay == 24) { // Condition if hour is 24 (Should be impossible but.)
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

    public static int getDayOfYear(Session session) {
        return getDayOfYear(session.getSessionStart());

    }

    public static int getDayOfYear(long date) {
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(date);
        return day.get(Calendar.DAY_OF_YEAR);
    }

    public static double getAveragePerDay(long after, long before, long total) {
        return total / getNumberOfDaysBetween(after, before);
    }

    public static long getNumberOfDaysBetween(long start, long end) {
        long value = 0;
        long test = start;
        long day = TimeAmount.DAY.ms();
        while (test < end) {
            test += day;
            value++;
        }
        return value;
    }

    public static void addMissingWorlds(WorldTimes worldTimes) {
        try {
            // Add 0 time for worlds not present.
            Set<String> nonZeroWorlds = worldTimes.getWorldTimes().keySet();
            for (String world : Database.getActive().fetch().getWorldNames(ServerInfo.getServerUUID())) {
                if (nonZeroWorlds.contains(world)) {
                    continue;
                }
                worldTimes.setGMTimesForWorld(world, new GMTimes());
            }
        } catch (DBException e) {
            Log.toLog(AnalysisUtils.class, e);
        }
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

    public static double calculateProbabilityOfStaying(Set<StickyData> stickyMonthData, Set<StickyData> stickyW, Set<StickyData> stickyStuckM, Set<StickyData> stickyStuckW, PlayerProfile playerProfile) {
        StickyData data = new StickyData(playerProfile);

        Set<StickyData> similarM = new HashSet<>();
        Set<StickyData> similarW = new HashSet<>();
        for (StickyData stickyData : stickyMonthData) {
            if (stickyData.distance(data) < 2.5) {
                similarM.add(stickyData);
            }
        }
        for (StickyData stickyData : stickyW) {
            if (stickyData.distance(data) < 2.5) {
                similarW.add(stickyData);
            }
        }

        double probability = 1.0;

        if (similarM.isEmpty() && similarW.isEmpty()) {
            return 0;
        }

        if (!similarM.isEmpty()) {
            int stickM = 0;
            for (StickyData stickyData : stickyStuckM) {
                if (similarM.contains(stickyData)) {
                    stickM++;
                }
            }
            probability *= (stickM / similarM.size());
        }

        if (!similarW.isEmpty()) {
            int stickW = 0;
            for (StickyData stickyData : stickyStuckW) {
                if (similarW.contains(stickyData)) {
                    stickW++;
                }
            }

            probability *= (stickW / similarW.size());
        }

        return probability;
    }

    public static TreeMap<Long, Map<String, Set<UUID>>> turnToActivityDataMap(long time, List<PlayerProfile> players) {
        TreeMap<Long, Map<String, Set<UUID>>> activityData = new TreeMap<>();
        if (!players.isEmpty()) {
            for (PlayerProfile player : players) {
                for (long date = time; date >= time - TimeAmount.MONTH.ms() * 2L; date -= TimeAmount.WEEK.ms()) {
                    ActivityIndex activityIndex = player.getActivityIndex(date);
                    String activityGroup = activityIndex.getGroup();

                    Map<String, Set<UUID>> map = activityData.getOrDefault(date, new HashMap<>());
                    Set<UUID> uuids = map.getOrDefault(activityGroup, new HashSet<>());
                    uuids.add(player.getUuid());
                    map.put(activityGroup, uuids);
                    activityData.put(date, map);
                }
            }
        }
        return activityData;
    }
}
