package main.java.com.djrapitops.plan.utilities.analysis;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.comparators.SessionLengthComparator;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * @param now
     * @param lastPlayed
     * @param playTime
     * @param loginTimes
     * @return
     */
    public static boolean isActive(long now, long lastPlayed, long playTime, int loginTimes) {
        int timeToActive = 10;
        long twoWeeks = 1209600000;
        return now - lastPlayed < twoWeeks
                && loginTimes > 3
                && playTime > 60 * timeToActive;
    }

    /**
     * @param registered
     * @param scale
     * @param now
     * @return
     */
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

    /**
     * @param data
     * @return
     */
    public static List<Long> transformSessionDataToLengths(Collection<Session> data) {
        return data.stream()
                .filter(Objects::nonNull)
                .filter(session -> session.getLength() > 0)
                .map(Session::getLength)
                .collect(Collectors.toList());
    }

    /**
     * @param analysisType
     * @param source
     * @param uuids
     * @return
     */
    public static String getTotal(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        if (analysisType == null) {
            return source.parseContainer("Err ", "Null Analysistype. ");
        }
        try {
            Number total;
            switch (analysisType) {
                case INT_TOTAL:
                    total = MathUtils.sumInt(getCorrectValues(uuids, source));
                    break;
                case LONG_TOTAL:
                    total = MathUtils.sumLong(getCorrectValues(uuids, source));
                    break;
                case LONG_TIME_MS_TOTAL:
                    total = MathUtils.sumLong(getCorrectValues(uuids, source));
                    return source.parseContainer(analysisType.getModifier(), FormatUtils.formatTimeAmount((long) total));
                case DOUBLE_TOTAL:
                    total = MathUtils.sumDouble(getCorrectValues(uuids, source));
                    break;
                default:
                    return source.parseContainer("", "Wrong Analysistype specified: " + analysisType.name());
            }
            return source.parseContainer(analysisType.getModifier(), String.valueOf(total));
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            return logPluginDataCausedError(source, e);
        }
    }

    private static Stream<Serializable> getCorrectValues(List<UUID> uuids, PluginData source) {
        return uuids.stream()
                .map(source::getValue)
                .filter(value -> !value.equals(-1))
                .filter(value -> !value.equals(-1L));
    }

    /**
     * @param analysisType
     * @param source
     * @param uuids
     * @return
     */
    public static String getAverage(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        if (analysisType == null) {
            return source.parseContainer("Err ", "Null Analysistype. ");
        }
        try {
            double average;
            switch (analysisType) {
                case LONG_EPOCH_MS_MINUS_NOW_AVG:
                    final long now = MiscUtils.getTime();
                    average = MathUtils.averageLong(getCorrectValues(uuids, source).map(value -> ((long) value) - now));
                    return source.parseContainer(analysisType.getModifier(), FormatUtils.formatTimeAmount((long) average));
                case LONG_AVG:
                    long averageLong = MathUtils.averageLong(getCorrectValues(uuids, source).map(i -> (Long) i));
                    return source.parseContainer(analysisType.getModifier(), String.valueOf(averageLong));
                case LONG_TIME_MS_AVG:
                    average = MathUtils.averageLong(getCorrectValues(uuids, source).map(i -> (Long) i));
                    return source.parseContainer(analysisType.getModifier(), FormatUtils.formatTimeAmount((long) average));
                case INT_AVG:
                    average = MathUtils.averageInt(getCorrectValues(uuids, source).map(i -> (Integer) i));
                    break;
                case DOUBLE_AVG:
                    average = MathUtils.averageDouble(getCorrectValues(uuids, source).map(i -> (Double) i));
                    break;
                default:
                    return source.parseContainer("Err ", "Wrong Analysistype specified: " + analysisType.name());
            }
            return source.parseContainer(analysisType.getModifier(), FormatUtils.cutDecimals(average));
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            return logPluginDataCausedError(source, e);
        }
    }

    /**
     * @param analysisType
     * @param source
     * @param uuids
     * @return
     */
    public static String getBooleanPercentage(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        if (analysisType != AnalysisType.BOOLEAN_PERCENTAGE) {
            return source.parseContainer("Err ", "Wrong Analysistype specified: " + analysisType.name());
        }

        try {
            List<Boolean> tempList = getCorrectValues(uuids, source)
                    .map(value -> (boolean) value)
                    .collect(Collectors.toList());
            long count = tempList.stream().filter(value -> value).count();
            return source.parseContainer(analysisType.getModifier(), (((double) count / tempList.size()) * 100) + "%");
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            return logPluginDataCausedError(source, e);
        }
    }

    /**
     * @param analysisType
     * @param source
     * @param uuids
     * @return
     */
    public static String getBooleanTotal(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        if (analysisType != AnalysisType.BOOLEAN_TOTAL) {
            return source.parseContainer("Err ", "Wrong Analysistype specified: " + analysisType.name());
        }

        try {
            List<Boolean> tempList = getCorrectValues(uuids, source)
                    .map(value -> (boolean) value)
                    .collect(Collectors.toList());
            long count = tempList.stream().filter(value -> value).count();
            return source.parseContainer(analysisType.getModifier(), count + " / " + tempList.size());
        } catch (Exception e) {
            return logPluginDataCausedError(source, e);
        }
    }

    private static String logPluginDataCausedError(PluginData source, Throwable e) {
        String placeholder = StringUtils.remove(source.getPlaceholder(""), '%');

        Log.error("A PluginData-source caused an exception: " + placeholder);
        Log.toLog("PluginData-source caused an exception: " + placeholder, e);
        return source.parseContainer("", "Exception during calculation.");
    }

    /**
     * Used to calculate unique players that have played within the time frame determined by scale.
     *
     * @param sessions All sessions sorted in a map by User's UUID
     * @param scale    Scale (milliseconds), time before (Current epoch - scale) will be ignored.
     * @return Amount of Unique joins within the time span.
     */
    public static int getUniqueJoins(Map<UUID, List<Session>> sessions, long scale) {
        long now = MiscUtils.getTime();
        long nowMinusScale = now - scale;

        Set<UUID> uniqueJoins = new HashSet<>();
        sessions.forEach((uuid, s) ->
                s.stream()
                        .filter(session -> session.getSessionStart() >= nowMinusScale)
                        .map(session -> uuid)
                        .forEach(uniqueJoins::add)
        );

        return uniqueJoins.size();
    }

    /**
     * @param sessions
     * @param scale
     * @return
     */
    public static int getUniqueJoinsPerDay(Map<UUID, List<Session>> sessions, long scale) {
        Map<Integer, Set<UUID>> uniqueJoins = new HashMap<>();
        long now = MiscUtils.getTime();
        long nowMinusScale = now - scale;

        sessions.forEach((uuid, s) -> {
            for (Session session : s) {
                if (scale != -1
                        && session.getSessionStart() < nowMinusScale) {
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

    public static long getNewUsersPerDay(List<Long> registers, long scale) {
        long now = MiscUtils.getTime();
        long nowMinusScale = now - scale;

        Set<Integer> days = new HashSet<>();
        for (Long date : registers) {
            if (scale != -1) {
                if (date < nowMinusScale) {
                    continue;
                }
                int day = getDayOfYear(date);
                days.add(day);
            }
        }

        long total = registers.stream().filter(date -> date >= nowMinusScale).count();
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
            if (hourOfDay == 24) { // Check if hour is 24 (Should be impossible but.)
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

    private static int getDayOfYear(Session session) {
        return getDayOfYear(session.getSessionStart());

    }

    private static int getDayOfYear(long date) {
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(date);
        return day.get(Calendar.DAY_OF_YEAR);
    }

    public static long getTotalPlaytime(List<Session> sessions) {
        return sessions.stream().mapToLong(Session::getLength).sum();
    }

    public static long getLongestSessionLength(List<Session> sessions) {
        Optional<Session> longest = sessions.stream().sorted(new SessionLengthComparator()).findFirst();
        return longest.map(Session::getLength).orElse(0L);
    }

    public static long getLastSeen(List<Session> userSessions) {
        OptionalLong max = userSessions.stream().mapToLong(Session::getSessionEnd).max();
        if (max.isPresent()) {
            return max.getAsLong();
        }
        return 0;
    }
}
