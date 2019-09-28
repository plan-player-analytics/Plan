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

import com.djrapitops.plan.delivery.domain.mutators.PlayersMutator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeolocationBarGraph extends BarGraph {

    GeolocationBarGraph(PlayersMutator mutator) {
        this(mutator.getGeolocations());
    }

    private GeolocationBarGraph(List<String> geolocations) {
        super(turnToBars(toGeolocationCounts(geolocations)));
    }

    GeolocationBarGraph(Map<String, Integer> geolocationCounts) {
        super(turnToBars(geolocationCounts));
    }

    private static List<Bar> turnToBars(Map<String, Integer> geolocationCounts) {
        return geolocationCounts.entrySet().stream()
                .map(entry -> new Bar(entry.getKey(), entry.getValue()))
                .sorted()
                .limit(20L)
                .collect(Collectors.toList());
    }

    private static Map<String, Integer> toGeolocationCounts(List<String> geolocations) {
        Map<String, Integer> counts = new HashMap<>();

        for (String geolocation : geolocations) {
            counts.put(geolocation, counts.getOrDefault(geolocation, 0) + 1);
        }
        return counts;
    }
}
