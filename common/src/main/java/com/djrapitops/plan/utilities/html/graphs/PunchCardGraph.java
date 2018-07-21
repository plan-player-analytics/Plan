/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.utilities.analysis.AnalysisUtils;

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

    private static int[][] turnIntoArray(List<int[]> daysAndHours) {
        int[][] dataArray = createEmptyArray();
        for (int[] dAndH : daysAndHours) {
            int d = dAndH[0];
            int h = dAndH[1];
            dataArray[d][h] = dataArray[d][h] + 1;
        }
        return dataArray;
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

    @Override
    public String toHighChartsSeries() {
        List<Long> sessionStarts = getSessionStarts(sessions);
        List<int[]> daysAndHours = AnalysisUtils.getDaysAndHours(sessionStarts);
        int[][] dataArray = turnIntoArray(daysAndHours);
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
}
