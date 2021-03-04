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

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Utility for getting a {@link java.util.TimeZone} from Plan {@link com.djrapitops.plan.settings.config.paths.FormatSettings#TIMEZONE} value.
 *
 * @author AuroraLS3
 */
public class TimeZoneUtility {

    private TimeZoneUtility() {
        // Static utility class
    }

    public static Optional<TimeZone> parseTimeZone(String value) {
        if ("server".equalsIgnoreCase(value)) return Optional.of(TimeZone.getDefault());

        try {
            ZoneId zoneId = ZoneId.of(value);
            return Optional.of(TimeZone.getTimeZone(zoneId));
        } catch (DateTimeException notFound) {
            return Optional.empty();
        }
    }

}