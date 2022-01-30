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

import com.djrapitops.plan.delivery.domain.mutators.PlayersMutator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * World Map that uses iso-a3 specification of Country codes.
 *
 * @author AuroraLS3
 */
public class WorldMap {

    private final Map<String, String> geoCodes;
    private final Map<String, Integer> geoCodeCounts;

    WorldMap(Map<String, String> geoCodes, PlayersMutator mutator) {
        this.geoCodes = geoCodes;
        this.geoCodeCounts = toGeoCodeCounts(mutator.getGeolocations());
    }

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

    private Map<String, Integer> toGeoCodeCounts(List<String> geoLocations) {
        Map<String, Integer> codeCounts = new HashMap<>();

        for (String geoLocation : geoLocations) {
            String countryCode = geoCodes.get(geoLocation.toLowerCase());
            codeCounts.put(countryCode, codeCounts.getOrDefault(countryCode, 0) + 1);
        }

        return codeCounts;
    }

    public List<Entry> getEntries() {
        return geoCodeCounts.entrySet().stream()
                .filter(entry -> entry.getValue() != 0)
                .map(e -> new Entry(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public static class Entry {
        private final String code;
        private final int value;

        public Entry(String code, int value) {
            this.code = code;
            this.value = value;
        }

        public String getCode() {
            return code;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;
            Entry entry = (Entry) o;
            return value == entry.value &&
                    Objects.equals(code, entry.code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code, value);
        }
    }
}
