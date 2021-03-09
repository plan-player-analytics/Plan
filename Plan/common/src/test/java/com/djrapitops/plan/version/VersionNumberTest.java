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
package com.djrapitops.plan.version;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VersionNumberTest {

    @Test
    void sortedNewestFirst() {
        List<VersionNumber> expected = Arrays.asList(
                new VersionNumber("5.0.9.f4.2423"),
                new VersionNumber("5.0.4.422345135431543"),
                new VersionNumber("1.5"),
                new VersionNumber("0")
        );
        List<VersionNumber> result = Arrays.asList(
                new VersionNumber("1.5"),
                new VersionNumber("0"),
                new VersionNumber("5.0.9.f4.2423"),
                new VersionNumber("5.0.4.422345135431543")
        );
        Collections.sort(result);
        assertEquals(expected, result);
    }

    @Test
    void isNewer() {
        assertTrue(new VersionNumber("1").isNewerThan(new VersionNumber("")));
        assertTrue(new VersionNumber("5").isNewerThan(new VersionNumber("4")));
        assertTrue(new VersionNumber("5").isNewerThan(new VersionNumber("4.0")));
        assertTrue(new VersionNumber("5").isNewerThan(new VersionNumber("4.0.999")));
        assertTrue(new VersionNumber("5.1.1").isNewerThan(new VersionNumber("5.1.0")));
        assertTrue(new VersionNumber("5.1.100").isNewerThan(new VersionNumber("5.1.1")));
        assertTrue(new VersionNumber("5.1 build 1034").isNewerThan(new VersionNumber("5.1 build 100")));
    }

    @Test
    void isNotNewer() {
        assertFalse(new VersionNumber("").isNewerThan(new VersionNumber("1")));
        assertFalse(new VersionNumber("4").isNewerThan(new VersionNumber("5")));
        assertFalse(new VersionNumber("4.0").isNewerThan(new VersionNumber("5")));
        assertFalse(new VersionNumber("4.0.999").isNewerThan(new VersionNumber("5")));
        assertFalse(new VersionNumber("5.1").isNewerThan(new VersionNumber("5.1.0")));
        assertFalse(new VersionNumber("5.1.0").isNewerThan(new VersionNumber("5.1.1")));
        assertFalse(new VersionNumber("5.1.0").isNewerThan(new VersionNumber("5.1.100")));
        assertFalse(new VersionNumber("5.1 build 100").isNewerThan(new VersionNumber("5.1 build 1034")));
    }

}
