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
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.mutators.PingMutator;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;

import java.util.*;

/**
 * Html table that displays countries and their average, worst and best pings.
 *
 * @author Rsl1122
 */
class PingTable extends TableContainer {

    private final Formatter<Double> decimalFormatter;

    PingTable(Map<String, List<Ping>> pingPerCountry, Formatter<Double> decimalFormatter) {
        super(
                Icon.called("globe") + " Country",
                Icons.SIGNAL + " Average Ping",
                Icons.SIGNAL + " Worst Ping",
                Icons.SIGNAL + " Best Ping"
        );
        this.decimalFormatter = decimalFormatter;
        setColor("amber");

        addRows(pingPerCountry);
    }

    private void addRows(Map<String, List<Ping>> pingPerCountry) {
        Map<String, Double> avg = new HashMap<>();
        Map<String, Integer> max = new HashMap<>();
        Map<String, Integer> min = new HashMap<>();

        for (Map.Entry<String, List<Ping>> entry : pingPerCountry.entrySet()) {
            PingMutator pingMutator = new PingMutator(entry.getValue());
            String country = entry.getKey();
            avg.put(country, pingMutator.average());
            max.put(country, pingMutator.max());
            min.put(country, pingMutator.min());
        }

        List<String> sortedKeys = new ArrayList<>(avg.keySet());
        Collections.sort(sortedKeys);

        for (String country : sortedKeys) {
            Double average = avg.get(country);
            Integer maximum = max.get(country);
            Integer minimum = min.get(country);
            addRow(
                    country,
                    average >= 0 ? decimalFormatter.apply(average) + " ms" : "-",
                    maximum >= 0 ? maximum + " ms" : "-",
                    minimum >= 0 ? minimum + " ms" : "-"
            );
        }
    }
}
