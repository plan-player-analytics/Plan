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
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
 * You can see the function plotted <a href="https://www.wolframalpha.com/input/?i=1+%2F+(x%2B1)+from+-1+to+2">here</a>
 * <p>
 * To fine tune the curve pi/2 is used since it felt like a good curve.
 * <p>
 * Activity index A is calculated by using the formula:
 * {@code A = 5 - 5 * [A(t1) + A(t2) + A(t3)] / 3}
 * <p>
 * <a href="https://www.wolframalpha.com/input/?i=plot+y+%3D+5+-+5+*+(1+%2F+(pi%2F2+*+x%2B1))+and+y+%3D1+and+y+%3D+2+and+y+%3D+3+and+y+%3D+3.75+from+-0.5+to+3">
 * Plot for A and limits
 * </a>
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

    public ActivityIndex(DataContainer container, long date, long playtimeMsThreshold) {
        this.playtimeMsThreshold = playtimeMsThreshold;

        this.date = date;
        value = calculate(container);
    }

    public ActivityIndex(List<FinishedSession> sessions, long date, long playtimeMsThreshold) {
        this.playtimeMsThreshold = playtimeMsThreshold;

        this.date = date;
        value = calculate(new SessionsMutator(sessions));
    }

    public ActivityIndex(double value, long date) {
        this.value = value;
        this.date = date;
    }

    public static String[] getDefaultGroups() {
        return getGroups(null);
    }

    public static String[] getDefaultGroupLangKeys() {
        return new String[]{
                HtmlLang.INDEX_VERY_ACTIVE.getKey(),
                HtmlLang.INDEX_ACTIVE.getKey(),
                HtmlLang.INDEX_REGULAR.getKey(),
                HtmlLang.INDEX_IRREGULAR.getKey(),
                HtmlLang.INDEX_INACTIVE.getKey()
        };
    }

    public static String[] getGroups(Locale locale) {
        if (locale == null) {
            return new String[]{
                    HtmlLang.INDEX_VERY_ACTIVE.getDefault(),
                    HtmlLang.INDEX_ACTIVE.getDefault(),
                    HtmlLang.INDEX_REGULAR.getDefault(),
                    HtmlLang.INDEX_IRREGULAR.getDefault(),
                    HtmlLang.INDEX_INACTIVE.getDefault()
            };
        }
        return new String[]{
                locale.getString(HtmlLang.INDEX_VERY_ACTIVE),
                locale.getString(HtmlLang.INDEX_ACTIVE),
                locale.getString(HtmlLang.INDEX_REGULAR),
                locale.getString(HtmlLang.INDEX_IRREGULAR),
                locale.getString(HtmlLang.INDEX_INACTIVE)
        };
    }

    public static String[] getGroupLocaleKeys() {
        return new String[]{
                HtmlLang.INDEX_VERY_ACTIVE.getKey(),
                HtmlLang.INDEX_ACTIVE.getKey(),
                HtmlLang.INDEX_REGULAR.getKey(),
                HtmlLang.INDEX_IRREGULAR.getKey(),
                HtmlLang.INDEX_INACTIVE.getKey()
        };
    }

    private double calculate(DataContainer container) {
        return calculate(SessionsMutator.forContainer(container));
    }

    private double calculate(SessionsMutator sessionsMutator) {
        if (sessionsMutator.all().isEmpty()) {
            return 0.0;
        }

        long week = TimeUnit.DAYS.toMillis(7L);
        long weekAgo = date - week;
        long twoWeeksAgo = date - 2L * week;
        long threeWeeksAgo = date - 3L * week;

        SessionsMutator weekOne = sessionsMutator.filterSessionsBetween(weekAgo, date);
        SessionsMutator weekTwo = sessionsMutator.filterSessionsBetween(twoWeeksAgo, weekAgo);
        SessionsMutator weekThree = sessionsMutator.filterSessionsBetween(threeWeeksAgo, twoWeeksAgo);

        double playtime1 = weekOne.toActivePlaytime();
        double playtime2 = weekTwo.toActivePlaytime();
        double playtime3 = weekThree.toActivePlaytime();

        double indexW1 = 1.0 / (Math.PI / 2.0 * (playtime1 / playtimeMsThreshold) + 1.0);
        double indexW2 = 1.0 / (Math.PI / 2.0 * (playtime2 / playtimeMsThreshold) + 1.0);
        double indexW3 = 1.0 / (Math.PI / 2.0 * (playtime3 / playtimeMsThreshold) + 1.0);

        double average = (indexW1 + indexW2 + indexW3) / 3.0;

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

    public double distance(ActivityIndex other) {
        // Logarithm makes the distance function more skewed towards active players
        // https://www.wolframalpha.com/input/?i=plot+y+%3D+log(5+-+5+*+(1+%2F+(pi%2F2+*+x%2B1)))+and+5+-+5+*+(1+%2F+(pi%2F2+*+x%2B1))+and+y+%3D1+and+y+%3D+2+and+y+%3D+3+and+y+%3D+3.75+from+-0.5+to+3
        return Math.abs(Math.log(other.value) - Math.log(value));
    }

    public static HtmlLang getGroupLang(double value) {
        if (value >= VERY_ACTIVE) {
            return HtmlLang.INDEX_VERY_ACTIVE;
        } else if (value >= ACTIVE) {
            return HtmlLang.INDEX_ACTIVE;
        } else if (value >= REGULAR) {
            return HtmlLang.INDEX_REGULAR;
        } else if (value >= IRREGULAR) {
            return HtmlLang.INDEX_IRREGULAR;
        } else {
            return HtmlLang.INDEX_INACTIVE;
        }
    }

    public static String getGroup(double value) {
        return getGroupLang(value).getDefault();
    }

    public String getGroup() {
        return getGroup(value);
    }

    public String getGroupLang() {
        return getGroupLang(value).getKey();
    }

    public String getGroup(Locale locale) {
        if (value >= VERY_ACTIVE) {
            return locale.getString(HtmlLang.INDEX_VERY_ACTIVE);
        } else if (value >= ACTIVE) {
            return locale.getString(HtmlLang.INDEX_ACTIVE);
        } else if (value >= REGULAR) {
            return locale.getString(HtmlLang.INDEX_REGULAR);
        } else if (value >= IRREGULAR) {
            return locale.getString(HtmlLang.INDEX_IRREGULAR);
        } else {
            return locale.getString(HtmlLang.INDEX_INACTIVE);
        }
    }

    public String getGroupLocaleKey() {
        if (value >= VERY_ACTIVE) {
            return HtmlLang.INDEX_VERY_ACTIVE.getKey();
        } else if (value >= ACTIVE) {
            return HtmlLang.INDEX_ACTIVE.getKey();
        } else if (value >= REGULAR) {
            return HtmlLang.INDEX_REGULAR.getKey();
        } else if (value >= IRREGULAR) {
            return HtmlLang.INDEX_IRREGULAR.getKey();
        } else {
            return HtmlLang.INDEX_INACTIVE.getKey();
        }
    }
}
