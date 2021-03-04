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

    @Test
    void utcIsValidZoneID() {
        ZoneId zone = ZoneId.of("UTC");
        assertNotNull(zone);
    }

    @Test
    void gmtPlusIsValidZoneID() {
        ZoneId zone = ZoneId.of("GMT+3");
        assertNotNull(zone);
    }

    @Test
    void gmtMinusIsValidZoneID() {
        ZoneId zone = ZoneId.of("GMT-3");
        assertNotNull(zone);
    }

    @Test
    void gmtPlusMinutesIsValidZoneID() {
        ZoneId zone = ZoneId.of("GMT+03:30");
        assertNotNull(zone);
    }

    @Test
    void gmtMinusMinutesIsValidZoneID() {
        ZoneId zone = ZoneId.of("GMT-03:30");
        assertNotNull(zone);
    }

    @Test
    void serverReturnsServerTimeZone() {
        Optional<TimeZone> result = TimeZoneUtility.parseTimeZone("server");
        assertTrue(result.isPresent());
        assertEquals(TimeZone.getDefault(), result.get());
    }

}