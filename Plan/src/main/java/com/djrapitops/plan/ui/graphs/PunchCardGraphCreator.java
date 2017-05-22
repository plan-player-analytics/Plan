/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.ui.graphs;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.SessionData;

/**
 *
 * @author Rsl1122
 */
public class PunchCardGraphCreator {

    public static String generateDataArray(Collection<SessionData> data) {
        // Initialize dataset
        List<Long> sessionStarts = getSessionStarts(data);
        List<int[]> daysAndHours = getDaysAndHours(sessionStarts);
        int[][] dataArray = createDataArray(daysAndHours);
        int big = findBiggestValue(dataArray);
        int[][] scaled = scale(dataArray, big);
        StringBuilder arrayBuilder = buildString(scaled);
        return arrayBuilder.toString();
    }

    private static StringBuilder buildString(int[][] scaled) {
        StringBuilder arrayBuilder = new StringBuilder();
        arrayBuilder.append("[");
        arrayBuilder.append("{").append("x:").append(-1).append(", y:").append(-1).append(", r:").append(1).append("}");
        arrayBuilder.append(",");
        arrayBuilder.append("{").append("x:").append(25).append(", y:").append(7).append(", r:").append(1).append("}");
        arrayBuilder.append(",");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                int value = scaled[i][j];
                if (value == 0) {
                    continue;
                }
                arrayBuilder.append("{").append("x:").append(j).append(", y:").append(i).append(", r:").append(value).append("}");
                if (!(i == 6 && j == 23)) {
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
        return dataArray;
    }

    private static List<int[]> getDaysAndHours(List<Long> sessionStarts) {
        List<int[]> daysAndHours = sessionStarts.stream().map(start -> {
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
        return daysAndHours;
    }

    private static List<Long> getSessionStarts(Collection<SessionData> data) {
        List<Long> sessionStarts = data.stream()
                .filter(s -> s != null)
                .filter(s -> s.isValid())
                .map(s -> s.getSessionStart())
                .sorted()
                .collect(Collectors.toList());
        return sessionStarts;
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
            Log.debug("Scaling: " + Arrays.toString(dataArray[i]) + " | " + big);
            for (int j = 0; j < 24; j++) {
                int value = (int) ((dataArray[i][j] * 10.0) / big);
                if (value != 0) {
                    value += 4;
                }
                scaled[i][j] = value;
            }
            Log.debug(" Scaled: " + Arrays.toString(scaled[i]));
        }

        return scaled;
    }
}
