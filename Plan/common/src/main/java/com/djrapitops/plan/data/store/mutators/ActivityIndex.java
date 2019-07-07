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
package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.List;
import java.util.Optional;

/**
 * Represents Activity index of a player at a certain date.
 * <p>
 * Old formula for activity index was not linear and difficult to turn into a query due to conditional multipliers.
 * Thus a new formula was written.
 * <p>
 * {@code T} - Time played after someone is considered active on a particular week
 * {@code t1, t2, t3} - Time played that week
 * <p>
 * Activity index takes into account last 3 weeks.
 * <p>
 * Activity for a single week is calculated using {@code A(t) = (1 / (pi/2 * (t/T) + 1))}.
 * A(t) is based on function f(x) = 1 / (x + 1), which has property f(0) = 1, decreasing from there, but not in a straight line.
 * You can see the function plotted here https://www.wolframalpha.com/input/?i=1+%2F+(x%2B1)+from+-1+to+2
 * <p>
 * To fine tune the curve pi/2 is used since it felt like a good curve.
 * <p>
 * Activity index A is calculated by using the formula:
 * {@code A = 5 - 5 * [A(t1) + A(t2) + A(t3)] / 3}
 * <p>
 * Plot for A and limits
 * https://www.wolframalpha.com/input/?i=plot+y+%3D+5+-+5+*+(1+%2F+(pi%2F2+*+x%2B1))+and+y+%3D1+and+y+%3D+2+and+y+%3D+3+and+y+%3D+3.75+from+-0.5+to+3
 * <p>
 * New Limits for A would thus be
 * {@code < 1: Inactive}
 * {@code > 1: Irregular}
 * {@code > 2: Regular}
 * {@code > 3: Active}
 * {@code > 3.75: Very Active}
 */
public class ActivityIndex {

    public static final double VERY_ACTIVE = 3.75;
    public static final double ACTIVE = 3.0;
    public static final double REGULAR = 2.0;
    public static final double IRREGULAR = 1.0;

    private final double value;
    private final long date;

    private long playtimeMsThreshold;

    public ActivityIndex(
            DataContainer container, long date,
            long playtimeMsThreshold
    ) {
        this.playtimeMsThreshold = playtimeMsThreshold;

        value = calculate(container, date);
        this.date = date;
    }

    public ActivityIndex(double value, long date) {
        this.value = value;
        this.date = date;
    }

    public static String[] getGroups() {
        return new String[]{"Very Active", "Active", "Regular", "Irregular", "Inactive"};
    }

    private double calculate(DataContainer container, long date) {
        long week = TimeAmount.WEEK.toMillis(1L);
        long weekAgo = date - week;
        long twoWeeksAgo = date - 2L * week;
        long threeWeeksAgo = date - 3L * week;

        Optional<List<Session>> sessionsValue = container.getValue(PlayerKeys.SESSIONS);
        if (!sessionsValue.isPresent()) {
            return 0.0;
        }
        SessionsMutator sessionsMutator = new SessionsMutator(sessionsValue.get());
        if (sessionsMutator.all().isEmpty()) {
            return 0.0;
        }

        SessionsMutator weekOne = sessionsMutator.filterSessionsBetween(weekAgo, date);
        SessionsMutator weekTwo = sessionsMutator.filterSessionsBetween(twoWeeksAgo, weekAgo);
        SessionsMutator weekThree = sessionsMutator.filterSessionsBetween(threeWeeksAgo, twoWeeksAgo);

        long playtime1 = weekOne.toActivePlaytime();
        long playtime2 = weekTwo.toActivePlaytime();
        long playtime3 = weekThree.toActivePlaytime();

        double A1 = 1.0 / (Math.PI / 2.0 * (playtime1 * 1.0 / playtimeMsThreshold) + 1.0);
        double A2 = 1.0 / (Math.PI / 2.0 * (playtime2 * 1.0 / playtimeMsThreshold) + 1.0);
        double A3 = 1.0 / (Math.PI / 2.0 * (playtime3 * 1.0 / playtimeMsThreshold) + 1.0);

        double average = (A1 + A2 + A3) / 3.0;

        return 5.0 - (5.0 * average);
    }

    public double getValue() {
        return value;
    }

    public long getDate() {
        return date;
    }

    public String getFormattedValue(Formatter<Double> formatter) {
        return formatter.apply(value);
    }

    public String getGroup() {
        if (value >= VERY_ACTIVE) {
            return "Very Active";
        } else if (value >= ACTIVE) {
            return "Active";
        } else if (value >= REGULAR) {
            return "Regular";
        } else if (value >= IRREGULAR) {
            return "Irregular";
        } else {
            return "Inactive";
        }
    }

    public String getColor() {
        if (value >= VERY_ACTIVE) {
            return "green";
        } else if (value >= ACTIVE) {
            return "green";
        } else if (value >= REGULAR) {
            return "lime";
        } else if (value >= IRREGULAR) {
            return "amber";
        } else {
            return "blue-gray";
        }
    }
}
