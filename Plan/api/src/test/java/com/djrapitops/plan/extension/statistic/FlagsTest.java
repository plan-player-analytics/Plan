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
package com.djrapitops.plan.extension.statistic;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlagsTest {

    @Test
    void storesTrueFalseAndMissingValues() {
        Flags flags = Flags.create()
                .put("enabled", true)
                .put("disabled", false)
                .put("unknown", null);

        Map<String, Boolean> valueByKey = flags.getValueByKey();
        assertTrue(valueByKey.get("enabled"));
        assertFalse(valueByKey.get("disabled"));
        assertTrue(valueByKey.containsKey("unknown"));
        assertNull(valueByKey.get("unknown"));
        assertFalse(flags.isEmpty());
    }

    @Test
    void nullOnlyFlagsAreNotEmpty() {
        Flags flags = Flags.create().put("unknown", null);

        assertFalse(flags.isEmpty());
        assertEquals(1, flags.getValueByKey().size());
    }
}
