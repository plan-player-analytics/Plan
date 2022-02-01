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

import com.djrapitops.plan.utilities.java.Maps;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test against exceptions due to unnamed geolocations.
 *
 * @author AuroraLS3
 */
class WorldMapTest {

    @Test
    void toGeolocationCountsCausesNoException() {
        Map<String, Integer> geolocations = new HashMap<>();
        geolocations.put("Finland", 1);
        geolocations.put("Sweden", 1);
        geolocations.put("Not Known", 1);
        geolocations.put("Local Machine", 1);
        geolocations.put("Denmark", 2);

        List<WorldMap.Entry> expected = Arrays.asList(
                new WorldMap.Entry("SWE", 1),
                new WorldMap.Entry("DNK", 2),
                new WorldMap.Entry("FIN", 1)
        );
        Map<String, String> geoCodes = Maps.builder(String.class, String.class)
                .put("finland", "FIN")
                .put("sweden", "SWE")
                .put("denmark", "DNK")
                .build();

        List<WorldMap.Entry> result = new WorldMap(geoCodes, geolocations).getEntries();
        assertEquals(expected, result);
    }

}