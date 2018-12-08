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

public class ActivityIndex {

    private final double value;

    private final long playtimeMsThreshold;
    private final int loginThreshold;

    public ActivityIndex(
            DataContainer container, long date,
            long playtimeMsThreshold, int loginThreshold
    ) {
        this.playtimeMsThreshold = playtimeMsThreshold;
        this.loginThreshold = loginThreshold;

        value = calculate(container, date);
    }

    public static String[] getGroups() {
        return new String[]{"Very Active", "Active", "Regular", "Irregular", "Inactive"};
    }

    private double calculate(DataContainer container, long date) {
        long week = TimeAmount.WEEK.toMillis(1L);
        long weekAgo = date - week;
        long twoWeeksAgo = date - 2L * week;
        long threeWeeksAgo = date - 3L * week;

        long activePlayThreshold = playtimeMsThreshold;
        int activeLoginThreshold = loginThreshold;

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

        // Playtime per week multipliers, max out to avoid too high values.
        double max = 4.0;

        long playtimeWeek = weekOne.toActivePlaytime();
        double weekPlay = (playtimeWeek * 1.0 / activePlayThreshold);
        if (weekPlay > max) {
            weekPlay = max;
        }
        long playtimeWeek2 = weekTwo.toActivePlaytime();
        double week2Play = (playtimeWeek2 * 1.0 / activePlayThreshold);
        if (week2Play > max) {
            week2Play = max;
        }
        long playtimeWeek3 = weekThree.toActivePlaytime();
        double week3Play = (playtimeWeek3 * 1.0 / activePlayThreshold);
        if (week3Play > max) {
            week3Play = max;
        }

        double playtimeMultiplier = 1.0;
        if (playtimeWeek + playtimeWeek2 + playtimeWeek3 > activePlayThreshold * 3.0) {
            playtimeMultiplier = 1.25;
        }

        // Reduce the harshness for new players and players who have had a vacation
        if (weekPlay > 1 && week3Play > 1 && week2Play == 0.0) {
            week2Play = 0.5;
        }
        if (weekPlay > 1 && week2Play == 0.0) {
            week2Play = 0.6;
        }
        if (weekPlay > 1 && week3Play == 0.0) {
            week3Play = 0.75;
        }

        double playAvg = (weekPlay + week2Play + week3Play) / 3.0;

        double weekLogin = weekOne.count() >= activeLoginThreshold ? 1.0 : 0.5;
        double week2Login = weekTwo.count() >= activeLoginThreshold ? 1.0 : 0.5;
        double week3Login = weekThree.count() >= activeLoginThreshold ? 1.0 : 0.5;

        double loginMultiplier = 1.0;
        double loginTotal = weekLogin + week2Login + week3Login;
        double loginAvg = loginTotal / 3.0;

        if (loginTotal <= 2.0) {
            // Reduce index for players that have not logged in the threshold amount for 2 weeks
            loginMultiplier = 0.75;
        }

        return playAvg * loginAvg * loginMultiplier * playtimeMultiplier;
    }

    public double getValue() {
        return value;
    }

    public String getFormattedValue(Formatter<Double> formatter) {
        return formatter.apply(value);
    }

    public String getGroup() {
        if (value >= 3.5) {
            return "Very Active";
        } else if (value >= 1.75) {
            return "Active";
        } else if (value >= 1.0) {
            return "Regular";
        } else if (value >= 0.5) {
            return "Irregular";
        } else {
            return "Inactive";
        }
    }

    public String getColor() {
        if (value >= 3.5) {
            return "green";
        } else if (value >= 1.75) {
            return "green";
        } else if (value >= 1.0) {
            return "lime";
        } else if (value >= 0.5) {
            return "amber";
        } else {
            return "blue-gray";
        }
    }
}
