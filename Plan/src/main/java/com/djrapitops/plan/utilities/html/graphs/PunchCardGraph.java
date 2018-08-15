/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Bubble Chart that represents login "punches" of players.
 *
 * @author Rsl1122
 * @since 4.2.0
 */
public class PunchCardGraph implements HighChart {

    private final Collection<Session> sessions;

    /**
     * Constuctor for the graph.
     *
     * @param sessions All sessions of All users this PunchCard represents.
     */
    public PunchCardGraph(Collection<Session> sessions) {
        this.sessions = sessions;
    }

    /*
     * First number signifies the Day of Week. (0 = Monday, 6 = Sunday)
     * Second number signifies the Hour of Day. (0 = 0 AM, 23 = 11 PM)
     */
    private static List<int[]> getDaysAndHours(Collection<Long> sessionStarts) {
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

    private static int[][] turnIntoArray(Collection<Long> sessionStarts) {
        List<int[]> daysAndHours = getDaysAndHours(sessionStarts);
        int[][] dataArray = createEmptyArray();
        for (int[] dAndH : daysAndHours) {
            int d = dAndH[0];
            int h = dAndH[1];
            dataArray[d][h] = dataArray[d][h] + 1;
        }
        return dataArray;
    }

    @Override
    public String toHighChartsSeries() {
        List<Long> sessionStarts = getSessionStarts(sessions);
        int[][] dataArray = turnIntoArray(sessionStarts);
        int big = findBiggestValue(dataArray);
        int[][] scaled = scale(dataArray, big);
        StringBuilder arrayBuilder = new StringBuilder("[");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                int value = scaled[i][j];
                if (j == 0) {
                    arrayBuilder.append("{x:").append(24 * 3600000);
                } else {
                    arrayBuilder.append("{x:").append(j * 3600000);
                }
                arrayBuilder.append(", y:").append(i)
                        .append(", z:").append(value).
                        append(", marker: { radius:").append(value)
                        .append("}}");
                if (i != 6 || j != 23) {
                    arrayBuilder.append(",");
                }
            }
        }
        arrayBuilder.append("]");
        return arrayBuilder.toString();
    }

    private static List<Long> getSessionStarts(Collection<Session> data) {
        return data.stream()
                .filter(Objects::nonNull)
                .map(s -> s.getUnsafe(SessionKeys.START))
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
