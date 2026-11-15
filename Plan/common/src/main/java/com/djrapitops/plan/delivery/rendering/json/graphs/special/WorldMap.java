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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * World Map that uses iso-a3 specification of Country codes.
 *
 * @author AuroraLS3
 */
public class WorldMap {

    private final Map<String, String> geoCodes;
    private final Map<String, Integer> geoCodeCounts;

    WorldMap(Map<String, String> geoCodes, Map<String, Integer> geolocationCounts) {
        this.geoCodes = geoCodes;
        this.geoCodeCounts = toGeoCodeCounts(geolocationCounts);
    }

    private Map<String, Integer> toGeoCodeCounts(Map<String, Integer> geolocationCounts) {
        Map<String, Integer> codeCounts = new HashMap<>();

        for (Map.Entry<String, Integer> entry : geolocationCounts.entrySet()) {
            String geolocation = entry.getKey().toLowerCase();
            String geoCode = geoCodes.get(geolocation);
            if (geoCode == null) {
                continue;
            }

            codeCounts.put(geoCode, entry.getValue());
        }

        return codeCounts;
    }

    public List<Entry> getEntries() {
        return geoCodeCounts.entrySet().stream()
                .filter(entry -> entry.getValue() != 0)
                .map(e -> new Entry(e.getKey(), e.getValue()))
                .toList();
    }

    public record Entry(String code, int value) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry entry)) return false;
            return value == entry.value &&
                    Objects.equals(code, entry.code);
        }

    }
}
