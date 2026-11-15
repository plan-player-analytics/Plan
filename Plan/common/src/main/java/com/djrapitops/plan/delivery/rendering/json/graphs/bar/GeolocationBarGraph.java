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
package com.djrapitops.plan.delivery.rendering.json.graphs.bar;

import java.util.List;
import java.util.Map;

public class GeolocationBarGraph extends BarGraph {

    GeolocationBarGraph(Map<String, Integer> geolocationCounts) {
        super(turnToBars(geolocationCounts));
    }

    private static List<Bar> turnToBars(Map<String, Integer> geolocationCounts) {
        return geolocationCounts.entrySet().stream()
                .map(entry -> new Bar(entry.getKey(), entry.getValue()))
                .sorted()
                .limit(20L)
                .toList();
    }
}
