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
package com.djrapitops.plan.system.delivery.rendering.json.graphs.special;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test against exceptions due to unnamed geolocations.
 *
 * @author Rsl1122
 */
@RunWith(JUnitPlatform.class)
class WorldMapTest {

    @Test
    void toGeolocationCountsCausesNoException() {
        Map<String, Integer> geolocations = new HashMap<>();
        geolocations.put("Finland", 1);
        geolocations.put("Sweden", 1);
        geolocations.put("Not Known", 1);
        geolocations.put("Local Machine", 1);
        geolocations.put("Denmark", 2);

        String expected = "[{'code':'SWE','value':1},{'code':'DNK','value':2},{'code':'FIN','value':1}]";
        String result = new WorldMap(geolocations).toHighChartsSeries();
        assertEquals(expected, result);
    }

}