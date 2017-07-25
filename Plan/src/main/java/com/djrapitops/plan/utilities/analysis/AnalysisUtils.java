package main.java.com.djrapitops.plan.utilities.analysis;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Rsl1122
 */
public class AnalysisUtils {

    /**
     *
     * @param now
     * @param lastPlayed
     * @param playTime
     * @param loginTimes
     * @return
     */
    public static boolean isActive(long now, long lastPlayed, long playTime, int loginTimes) {
        int timeToActive = Settings.ANALYSIS_MINUTES_FOR_ACTIVE.getNumber();
        if (timeToActive < 0) {
            timeToActive = 0;
        }
        long twoWeeks = 1209600000;
        if (now - lastPlayed < twoWeeks) {
            if (loginTimes > 3) {
                if (playTime > 60 * timeToActive) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param registered
     * @param scale
     * @param now
     * @return
     */
    public static int getNewPlayers(List<Long> registered, long scale, long now) {
        int newPlayers = 0;
        if (!registered.isEmpty()) {
            newPlayers = registered.stream()
                    .filter((reg) -> (reg != null))
                    .filter((reg) -> (reg > now - scale))
                    .map((_item) -> 1).reduce(newPlayers, Integer::sum);
        }
        // Filters out register dates before scale
        return newPlayers;
    }

    /**
     *
     * @param data
     * @return
     */
    public static List<Long> transformSessionDataToLengths(Collection<SessionData> data) {
        return data.stream()
                .filter(Objects::nonNull)
                .filter(SessionData::isValid)
                .map(SessionData::getLength)
                .collect(Collectors.toList());
    }

    /**
     *
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
            return source.parseContainer(analysisType.getModifier(), total + "");
        } catch (Throwable e) {
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
     *
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
                    return source.parseContainer(analysisType.getModifier(), averageLong + "");
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
        } catch (Throwable e) {
            return logPluginDataCausedError(source, e);
        }
    }

    /**
     *
     * @param analysisType
     * @param source
     * @param uuids
     * @return
     */
    public static String getBooleanPercentage(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        if (analysisType == AnalysisType.BOOLEAN_PERCENTAGE) {
            try {
                List<Boolean> tempList = getCorrectValues(uuids, source)
                        .map(value -> (boolean) value)
                        .collect(Collectors.toList());
                long count = tempList.stream().filter(value -> value).count();
                return source.parseContainer(analysisType.getModifier(), ((double) (count / tempList.size()) * 100) + "%");
            } catch (Throwable e) {
                return logPluginDataCausedError(source, e);
            }
        }
        return source.parseContainer("Err ", "Wrong Analysistype specified: " + analysisType.name());
    }

    /**
     *
     * @param analysisType
     * @param source
     * @param uuids
     * @return
     */
    public static String getBooleanTotal(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        if (analysisType == AnalysisType.BOOLEAN_TOTAL) {
            try {
                List<Boolean> tempList = getCorrectValues(uuids, source)
                        .map(value -> (boolean) value)
                        .collect(Collectors.toList());
                long count = tempList.stream().filter(value -> value).count();
                return source.parseContainer(analysisType.getModifier(), count + " / " + tempList.size());
            } catch (Throwable e) {
                return logPluginDataCausedError(source, e);
            }
        }
        return source.parseContainer("Err ", "Wrong Analysistype specified: " + analysisType.name());
    }

    private static String logPluginDataCausedError(PluginData source, Throwable e) {
        Log.error("A PluginData-source caused an exception: " + source.getPlaceholder("").replace("%", ""));
        Log.toLog("A PluginData-source caused an exception: " + source.getPlaceholder("").replace("%", ""), Log.getErrorsFilename());
        Log.toLog("com.djrapitops.plan.utilities.AnalysisUtils", e);
        return source.parseContainer("", "Exception during calculation.");
    }

    /**
     *
     * @param sessions
     * @param scale
     * @return
     */
    public static int getUniqueJoins(Map<UUID, List<SessionData>> sessions, long scale) {
        long now = MiscUtils.getTime();
        long nowMinusScale = now - scale;
        Set<UUID> uniqueJoins = new HashSet<>();
        sessions.keySet().forEach((uuid) -> {
            List<SessionData> s = sessions.get(uuid);
            for (SessionData session : s) {
                if (session.getSessionStart() < nowMinusScale) {
                    continue;
                }
                uniqueJoins.add(uuid);
            }
        });
        return uniqueJoins.size();
    }

    /**
     *
     * @param sessions
     * @param scale
     * @return
     */
    public static int getUniqueJoinsPerDay(Map<UUID, List<SessionData>> sessions, long scale) {
        Map<Integer, Set<UUID>> uniqueJoins = new HashMap<>();
        long now = MiscUtils.getTime();
        long nowMinusScale = now - scale;
        sessions.keySet().forEach((uuid) -> {
            List<SessionData> s = sessions.get(uuid);
            for (SessionData session : s) {
                if (scale != -1) {
                    if (session.getSessionStart() < nowMinusScale) {
                        continue;
                    }
                }
                int day = getDayOfYear(session);
                if (!uniqueJoins.containsKey(day)) {
                    uniqueJoins.put(day, new HashSet<>());
                }
                uniqueJoins.get(day).add(uuid);
            }
        });
        int total = MathUtils.sumInt(uniqueJoins.values().stream().map(Set::size));
        int size = uniqueJoins.keySet().size();
        if (size == 0) {
            return 0;
        }
        return total / size;
    }

    /**
     *
     * @param sessionStarts
     * @return
     */
    public static List<int[]> getDaysAndHours(List<Long> sessionStarts) {
        return sessionStarts.stream().map((Long start) -> {
            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(start);
            int hourOfDay = day.get(Calendar.HOUR_OF_DAY);
            int dayOfWeek = day.get(Calendar.DAY_OF_WEEK) - 2;
            if (hourOfDay == 24) {
                hourOfDay = 0;
                dayOfWeek += 1;
            }
            if (dayOfWeek > 6) {
                dayOfWeek = 0;
            }
            if (dayOfWeek < 0) {
                dayOfWeek = 6;
            }
            return new int[]{dayOfWeek, hourOfDay};
        }).collect(Collectors.toList());
    }

    private static int getDayOfYear(SessionData session) {
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(session.getSessionStart());
        return day.get(Calendar.DAY_OF_YEAR);
    }
}
