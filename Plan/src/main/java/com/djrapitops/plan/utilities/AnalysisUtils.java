package main.java.com.djrapitops.plan.utilities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.tables.SortableCommandUseTableCreator;
import main.java.com.djrapitops.plan.ui.tables.SortablePlayersTableCreator;

/**
 *
 * @author Rsl1122
 */
public class AnalysisUtils {

    /**
     *
     * @param lastPlayed
     * @param playTime
     * @param loginTimes
     * @return
     */
    public static boolean isActive(long lastPlayed, long playTime, int loginTimes) {
        int timeToActive = Settings.ANALYSIS_MINUTES_FOR_ACTIVE.getNumber();
        if (timeToActive < 0) {
            timeToActive = 0;
        }
        long twoWeeks = 1209600000;
        if (new Date().getTime() - lastPlayed < twoWeeks) {
            if (loginTimes > 3) {
                if (playTime > 60 * timeToActive) {
                    return true;
                }
            }
        }
        return false;
    }

    static String createTableOutOfMap(Map<String, Integer> commandUse) {
        return SortableCommandUseTableCreator.createSortedCommandUseTable(commandUse);
    }

    static String createSortablePlayersTable(Collection<UserData> data) {
        return SortablePlayersTableCreator.createSortablePlayersTable(data);
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
        List<Long> list = data.stream()
                .filter(session -> session != null)
                .filter(session -> session.isValid())
                .map(session -> session.getLength())
                .collect(Collectors.toList());
        return list;
    }

    /**
     *
     * @param list
     * @return
     */
    public static long average(Collection<Long> list) {
        if (list.isEmpty()) {
            return 0;
        }
        long total = 0;
        for (Long long1 : list) {
            total += long1;
        }
        return total / list.size();
    }

    // Refactor to MathUtils class
    public static String getTotal(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        try {
            if (null != analysisType) {
                Number total;
                switch (analysisType) {
                    case INT_TOTAL:
                        total = getCorrectValues(uuids, source)
                                .mapToInt(value -> (Integer) value)
                                .sum();
                        break;
                    case LONG_TOTAL:
                        total = getCorrectValues(uuids, source)
                                .mapToLong(value -> (Long) value)
                                .sum();
                        break;
                    case LONG_TIME_MS_TOTAL:
                        total = getCorrectValues(uuids, source)
                                .mapToLong(value -> (Long) value)
                                .sum();
                        return source.parseContainer(analysisType.getModifier(), FormatUtils.formatTimeAmount(total + ""));
                    case DOUBLE_TOTAL:
                        total = getCorrectValues(uuids, source)
                                .mapToDouble(value -> (Double) value)
                                .sum();
                        break;
                    default:
                        return source.parseContainer("ERROR ", "Wrong Analysistype specified: " + analysisType.name());
                }
                return source.parseContainer(analysisType.getModifier(), total + "");
            }
        } catch (Throwable e) {
            Log.toLog("com.djrapitops.plan.utilities.AnalysisUtils", e);
        }
        return source.parseContainer("ERROR ", "Exception during total calculation.");
    }

    private static Stream<Serializable> getCorrectValues(List<UUID> uuids, PluginData source) {
        return uuids.stream()
                .map(uuid -> source.getValue(uuid))
                .filter(value -> !value.equals(-1));
    }

    // Refactor to MathUtils class
    public static String getAverage(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        try {
            if (null != analysisType) {
                Number total;
                switch (analysisType) {
                    case LONG_EPOCH_MS_MINUS_NOW_AVG:
                        final long now = new Date().getTime();
                        List<Long> longValues = getCorrectValues(uuids, source)
                                .map(value -> ((long) value) - now)
                                .collect(Collectors.toList());
                        return source.parseContainer(analysisType.getModifier(), FormatUtils.formatTimeAmount(average(longValues) + ""));
                    case INT_AVG:
                        OptionalDouble avg = getCorrectValues(uuids, source)
                                .map(value -> (Integer) value)
                                .mapToInt(i -> i)
                                .average();
                        if (!avg.isPresent()) {
                            total = 0;
                        } else {
                            total = avg.getAsDouble();
                        }
                        break;
                    case LONG_AVG:
                        List<Long> longVal = getCorrectValues(uuids, source)
                                .map(value -> (Long) value)
                                .collect(Collectors.toList());
                        total = average(longVal);
                        break;
                    case LONG_TIME_MS_AVG:
                        List<Long> longVal2 = getCorrectValues(uuids, source)
                                .map(value -> (Long) value)
                                .collect(Collectors.toList());
                        return source.parseContainer(analysisType.getModifier(), FormatUtils.formatTimeAmount(average(longVal2) + ""));
                    case DOUBLE_AVG:
                        OptionalDouble average = getCorrectValues(uuids, source)
                                .mapToDouble(value -> (Double) value)
                                .average();
                        if (!average.isPresent()) {
                            total = 0;
                        } else {
                            total = average.getAsDouble();
                        }
                        break;
                    default:
                        return source.parseContainer("", "Wrong Analysistype specified: " + analysisType.name());
                }
                return source.parseContainer(analysisType.getModifier(), total + "");
            }
        } catch (Throwable e) {
            Log.error("A PluginData-source caused an exception: "+source.getPlaceholder("").replace("%", ""));
            Log.toLog("com.djrapitops.plan.utilities.AnalysisUtils", e);
        }
        return source.parseContainer("", "Exception during average calculation.");
    }

    static String getBooleanPercentage(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        if (analysisType == AnalysisType.BOOLEAN_PERCENTAGE) {
            try {
                List<Boolean> tempList = getCorrectValues(uuids, source)
                        .map(value -> (boolean) value)
                        .collect(Collectors.toList());
                long count = tempList.stream().filter(value -> value).count();
                return source.parseContainer(analysisType.getModifier(), ((double) (count / tempList.size()) * 100) + "%");
            } catch (Throwable e) {
                Log.error("A PluginData-source caused an exception: "+source.getPlaceholder("").replace("%", ""));
                Log.toLog("com.djrapitops.plan.utilities.AnalysisUtils", e);
                return source.parseContainer("", "Exception during calculation.");
            }
        }
        return source.parseContainer("", "Wrong Analysistype specified: " + analysisType.name());
    }

    static String getBooleanTotal(AnalysisType analysisType, PluginData source, List<UUID> uuids) {
        if (analysisType == AnalysisType.BOOLEAN_TOTAL) {
            try {
                List<Boolean> tempList = getCorrectValues(uuids, source)
                        .map(value -> (boolean) value)
                        .collect(Collectors.toList());
                long count = tempList.stream().filter(value -> value).count();
                return source.parseContainer(analysisType.getModifier(), count + " / " + tempList.size());
            } catch (Throwable e) {
                Log.error("A PluginData-source caused an exception: "+source.getPlaceholder("").replace("%", ""));
                Log.toLog("com.djrapitops.plan.utilities.AnalysisUtils", e);
                return source.parseContainer("", "Exception during calculation.");
            }
        }
        return source.parseContainer("", "Wrong Analysistype specified: " + analysisType.name());
    }
}
