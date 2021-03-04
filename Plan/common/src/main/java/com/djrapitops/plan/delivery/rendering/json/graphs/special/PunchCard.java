/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.rendering.json.graphs.special;

import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;

import java.util.*;

/**
 * Bubble Chart that represents login "punches" of players.
 *
 * @author AuroraLS3
 */
public class PunchCard {

    private final SessionsMutator sessions;
    private final TimeZone timeZone;

    /**
     * Constructor for the graph.
     *
     * @param sessions All sessions of All users this PunchCard represents.
     * @param timeZone TimeZone to use for the hour grouping.
     */
    PunchCard(SessionsMutator sessions, TimeZone timeZone) {
        this.sessions = sessions;
        this.timeZone = timeZone;
    }

    /*
     * First number signifies the Day of Week. (0 = Monday, 6 = Sunday)
     * Second number signifies the Hour of Day. (0 = 0 AM, 23 = 11 PM)
     */
    private int[][] getDaysAndHours(Collection<Long> sessionStarts) {
        return sessionStarts.stream().map((Long start) -> {
            Calendar day = Calendar.getInstance(timeZone);
            day.setTimeInMillis(start);
            int hourOfDay = day.get(Calendar.HOUR_OF_DAY); // 0 AM is 0
            int dayOfWeek = day.get(Calendar.DAY_OF_WEEK) - 2; // Monday is 0, Sunday is -1
            if (dayOfWeek > 6) { // If Hour added a day on Sunday, move to Monday
                dayOfWeek = 0;
            }
            if (dayOfWeek < 0) { // Move Sunday to 6
                dayOfWeek = 6;
            }
            return new int[]{dayOfWeek, hourOfDay};
        }).toArray(int[][]::new);
    }

    private int[][] turnIntoMatrix(Collection<Long> sessionStarts) {
        int[][] daysAndHours = getDaysAndHours(sessionStarts);
        int[][] matrix = createZeroMatrix();
        for (int[] dayAndHour : daysAndHours) {
            int day = dayAndHour[0];
            int hour = dayAndHour[1];
            matrix[day][hour] = matrix[day][hour] + 1;
        }
        return matrix;
    }

    public List<Dot> getDots() {
        List<Dot> dots = new ArrayList<>();

        List<Long> sessionStarts = sessions.toSessionStarts();

        int[][] dayHourMatrix = turnIntoMatrix(sessionStarts);
        int big = findBiggestValue(dayHourMatrix);
        int[][] scaled = scale(dayHourMatrix, big);

        for (int day = 0; day < 7; day++) {
            for (int hour = 0; hour < 24; hour++) {
                int value = scaled[day][hour];

                int x = hour * 3600000;

                dots.add(new Dot(x, day, value, value));
            }
        }

        return dots;
    }

    private int[][] createZeroMatrix() {
        int[][] dataArray = new int[7][24];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                dataArray[i][j] = 0;
            }
        }
        return dataArray;
    }

    private int findBiggestValue(int[][] dataArray) {
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

    private int[][] scale(int[][] dataArray, int big) {
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

    public static class Dot {
        final int x;
        final int y;
        final int z;
        final Marker marker;

        public Dot(int x, int y, int z, int radius) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.marker = new Marker(radius);
        }

        @Override
        public String toString() {
            return "{" +
                    "x:" + x +
                    ", y:" + y +
                    ", z:" + z +
                    ", marker:" + marker +
                    '}';
        }

        public static class Marker {
            final int radius;

            Marker(int radius) {
                this.radius = radius;
            }

            @Override
            public String toString() {
                return "{" +
                        "radius:" + radius +
                        '}';
            }
        }
    }
}
