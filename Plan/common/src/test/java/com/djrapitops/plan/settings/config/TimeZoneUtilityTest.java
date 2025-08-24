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
package com.djrapitops.plan.settings.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sanity Test for different timezones.
 *
 * @author AuroraLS3
 */
class TimeZoneUtilityTest {

    @ParameterizedTest
    @CsvSource({
            "UTC",
            "GMT+3",
            "GMT-3",
            "GMT+03:30",
            "GMT-03:30"
    })
    void isValidZoneId(String zoneId) {
        ZoneId zone = ZoneId.of(zoneId);
        assertNotNull(zone);
    }

    @Test
    void serverReturnsServerTimeZone() {
        Optional<TimeZone> result = TimeZoneUtility.parseTimeZone("server");
        assertTrue(result.isPresent());
        assertEquals(TimeZone.getDefault(), result.get());
    }

}