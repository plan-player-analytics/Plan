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
package com.djrapitops.plan.delivery.domain.mutators.formatting;

import com.djrapitops.plan.delivery.formatting.time.TimeAmountFormatter;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.FormatSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link TimeAmountFormatter} that checks extra zeros config example.
 *
 * @author AuroraLS3
 */
class TimeAmountFormatterDefaultTest {

    private static TimeAmountFormatter underTest;

    @BeforeAll
    static void setUpFormatter() {
        PlanConfig config = Mockito.mock(PlanConfig.class);
        when(config.get(FormatSettings.YEAR)).thenReturn("1 year, ");
        when(config.get(FormatSettings.YEARS)).thenReturn("%years% years, ");
        when(config.get(FormatSettings.MONTH)).thenReturn("1 month, ");
        when(config.get(FormatSettings.YEAR)).thenReturn("1 year, ");
        when(config.get(FormatSettings.MONTH)).thenReturn("1 month, ");
        when(config.get(FormatSettings.MONTHS)).thenReturn("%months% months, ");
        when(config.get(FormatSettings.DAY)).thenReturn("1d ");
        when(config.get(FormatSettings.DAYS)).thenReturn("%days%d ");
        when(config.get(FormatSettings.HOURS)).thenReturn("%hours%h ");
        when(config.get(FormatSettings.MINUTES)).thenReturn("%minutes%m ");
        when(config.get(FormatSettings.SECONDS)).thenReturn("%seconds%s");
        when(config.get(FormatSettings.ZERO_SECONDS)).thenReturn("0s");
        underTest = new TimeAmountFormatter(config);
    }

    @Test
    void exampleOne() {
        String expected = "1 year, 1 month, 5d 12h 30m 20s";

        long ms = TimeUnit.DAYS.toMillis(400L) +
                TimeUnit.HOURS.toMillis(12L) +
                TimeUnit.MINUTES.toMillis(30L) +
                TimeUnit.SECONDS.toMillis(20L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    void exampleTwo() {
        String expected = "1 year, 1 month, 5d ";

        long ms = TimeUnit.DAYS.toMillis(400L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    void exampleThree() {
        String expected = "12h 20s";

        long ms = TimeUnit.HOURS.toMillis(12L) +
                TimeUnit.SECONDS.toMillis(20L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    void exampleFour() {
        String expected = "30m ";

        long ms = TimeUnit.MINUTES.toMillis(30L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    void exampleFive() {
        String expected = "20s";

        long ms = TimeUnit.SECONDS.toMillis(20L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    void exampleZero() {
        String expected = "0s";

        long ms = 0L;
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    void exampleOneSecond() {
        String expected = "1s";

        long ms = TimeUnit.SECONDS.toMillis(1L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    void exampleOneMinute() {
        String expected = "1m ";

        long ms = TimeUnit.MINUTES.toMillis(1L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

}