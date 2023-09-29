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
package com.djrapitops.plan.settings.locale;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocaleSystemTest {

    @Test
    void noKeyCollisions() {
        assertDoesNotThrow(LocaleSystem::getKeys);
    }

    @Test
    void noIdentifierCollisions() {
        assertDoesNotThrow(LocaleSystem::getIdentifiers);
    }

    @Test
    void noIdentifierParentValues() {
        Set<String> keys = LocaleSystem.getKeys().keySet();
        List<String> invalidParentKeys = new ArrayList<>();
        for (String key : keys) {
            for (String key2 : keys) {
                if (!key.equals(key2) && key.contains(key2) && key.replace(key2, "").startsWith(".")) {
                    invalidParentKeys.add("'" + key2 + "' is the parent of '" + key + "' but has a value\n");
                }
            }
        }
        assertTrue(invalidParentKeys.isEmpty(), invalidParentKeys::toString);
    }
}