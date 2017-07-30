/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class PunchCardGraphCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private PunchCardGraphCreator() {
        throw new IllegalStateException("Utility class");
    }


    public static String createDataSeries(Collection<SessionData> sessions) {
        List<Long> sessionStarts = getSessionStarts(sessions);
        List<int[]> daysAndHours = AnalysisUtils.getDaysAndHours(sessionStarts);
        int[][] dataArray = createDataArray(daysAndHours);
        int big = findBiggestValue(dataArray);
        int[][] scaled = scale(dataArray, big);
        StringBuilder arrayBuilder = new StringBuilder("[");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                int value = scaled[i][j];
                if (value == 0) {
                    continue;
                }
                arrayBuilder.append("{x:").append(j * 3600000).append(", y:").append(i).append(", z:").append(value).append(", marker: { radius:").append(value).append("}}");
                if (i != 6 || j != 23) {
                    arrayBuilder.append(",");
                }
            }
        }
        arrayBuilder.append("]");
        return arrayBuilder.toString();
    }

    /**
     * @param data
     * @return
     */
    public static String generateDataArray(Collection<SessionData> data) {
        List<Long> sessionStarts = getSessionStarts(data);
        List<int[]> daysAndHours = AnalysisUtils.getDaysAndHours(sessionStarts);
        int[][] dataArray = createDataArray(daysAndHours);
        int big = findBiggestValue(dataArray);
        int[][] scaled = scale(dataArray, big);
        StringBuilder arrayBuilder = buildString(scaled);
        return arrayBuilder.toString();
    }

    private static StringBuilder buildString(int[][] scaled) {
        StringBuilder arrayBuilder = new StringBuilder("[");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                int value = scaled[i][j];
                if (value == 0) {
                    continue;
                }
                arrayBuilder.append("{x:").append(j).append(", y:").append(i).append(", r:").append(value).append("}");
                if (i != 6 || j != 23) {
                    arrayBuilder.append(",");
                }
            }
        }
        arrayBuilder.append("]");
        return arrayBuilder;
    }

    private static int[][] createDataArray(List<int[]> daysAndHours) {
        int[][] dataArray = createEmptyArray();
        for (int[] dAndH : daysAndHours) {
            int d = dAndH[0];
            int h = dAndH[1];
            dataArray[d][h] = dataArray[d][h] + 1;
        }

        if (Settings.ANALYSIS_REMOVE_OUTLIERS.isTrue()) {
            int avg = findAverage(dataArray);
            double standardDeviation = getStandardDeviation(dataArray, avg);
            if (standardDeviation > 3.5) {
                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < 24; j++) {
                        int value = dataArray[i][j];
                        if (value - avg > 3 * standardDeviation) {
                            dataArray[i][j] = avg;
                        }
                    }
                }
            }
        }
        return dataArray;
    }

    private static double getStandardDeviation(int[][] array, int avg) {
        int[][] valueMinusAvg = new int[7][24];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                valueMinusAvg[i][j] = (int) Math.pow(Math.abs(array[i][j] - avg), 2);
            }
        }

        double size = array.length * array[0].length;
        double sum = sum(valueMinusAvg);
        return Math.sqrt(sum / size);
    }

    private static int findAverage(int[][] array) {
        int total = sum(array);
        int size = array.length * array[0].length;
        return (int) MathUtils.average(total, size);
    }

    private static int sum(int[][] array) {
        return Arrays.stream(array).mapToInt(is -> is.length).sum();
    }

    private static List<Long> getSessionStarts(Collection<SessionData> data) {
        long now = MiscUtils.getTime();
        return data.stream()
                .filter(Objects::nonNull)
                .filter(SessionData::isValid)
                .map(SessionData::getSessionStart)
                .filter(start -> now - start < (long) 2592000 * (long) 1000)
                .sorted()
                .collect(Collectors.toList());
    }

    private static int[][] createEmptyArray() {
        int[][] dataArray = new int[7][24];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                dataArray[i][j] = 0;
            }
        }
        return dataArray;
    }

    private static int findBiggestValue(int[][] dataArray) {
        int highest = 1;
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                int num = dataArray[i][j];
                if (num > highest) {
                    highest = num;
                }
            }
        }
        return highest;
    }

    private static int[][] scale(int[][] dataArray, int big) {

        int[][] scaled = new int[7][24];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                int value = (int) ((dataArray[i][j] * 10.0) / big);
                if (value != 0) {
                    value += 4;
                }
                scaled[i][j] = value;
            }
        }
        return scaled;
    }
}
