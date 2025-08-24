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
package com.djrapitops.plan.delivery.formatting.time;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.function.Executable;
import utilities.RandomData;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests against https://github.com/plan-player-analytics/Plan/issues/1239 issues.
 *
 * @author AuroraLS3
 */
class ISO8601NoClockTZIndependentFormatterTest {

    ISO8601NoClockTZIndependentFormatter formatter = new ISO8601NoClockTZIndependentFormatter();

    @RepeatedTest(10)
    @DisplayName("OS timezone is not applied to Formatter")
    void osTimeZoneIsNotApplied() {
        long startOfDay = RandomData.randomTimeAfter(0L);
        startOfDay = startOfDay - (startOfDay % TimeUnit.DAYS.toMillis(1));

        ZonedDateTime epoch = Instant.EPOCH.atZone(ZoneId.of("UTC"));
        ZonedDateTime date = epoch.plusSeconds(TimeUnit.MILLISECONDS.toSeconds(startOfDay));
        String expected = date.getYear() + "-" +
                StringUtils.leftPad("" + date.getMonthValue(), 2, '0') + "-" +
                StringUtils.leftPad("" + date.getDayOfMonth(), 2, '0');

        List<Executable> assertions = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            String result = formatter.apply(startOfDay + TimeUnit.HOURS.toMillis(i));
            final int added = i;
            assertions.add(() -> assertEquals(expected, result, "Incorrect result when added " + added + " hours"));
        }
        assertAll(assertions);
    }
}