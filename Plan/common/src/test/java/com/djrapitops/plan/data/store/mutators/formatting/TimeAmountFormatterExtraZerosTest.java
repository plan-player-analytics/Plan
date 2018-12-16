package com.djrapitops.plan.data.store.mutators.formatting;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.FormatSettings;
import com.djrapitops.plan.utilities.formatting.time.TimeAmountFormatter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link TimeAmountFormatter} that checks extra zeros config example.
 *
 * @author Rsl1122
 */
public class TimeAmountFormatterExtraZerosTest {

    private static TimeAmountFormatter underTest;

    @BeforeClass
    public static void setUpClass() {
        PlanConfig config = Mockito.mock(PlanConfig.class);
        when(config.get(FormatSettings.YEAR)).thenReturn("1 year, ");
        when(config.get(FormatSettings.YEARS)).thenReturn("%years% years, ");
        when(config.get(FormatSettings.MONTH)).thenReturn("1 month, ");
        when(config.get(FormatSettings.MONTHS)).thenReturn("%months% months, ");
        when(config.get(FormatSettings.DAY)).thenReturn("1d ");
        when(config.get(FormatSettings.DAYS)).thenReturn("%days%d ");
        when(config.get(FormatSettings.HOURS)).thenReturn("%zero%%hours%:");
        when(config.get(FormatSettings.MINUTES)).thenReturn("%hours%%zero%%minutes%:");
        when(config.get(FormatSettings.SECONDS)).thenReturn("%minutes%%zero%%seconds%");
        when(config.get(FormatSettings.ZERO_SECONDS)).thenReturn("00:00:00");
        underTest = new TimeAmountFormatter(config);
    }

    @Test
    public void exampleOne() {
        String expected = "1 year, 1 month, 5d 12:30:20";

        long ms = TimeUnit.DAYS.toMillis(400L) +
                TimeUnit.HOURS.toMillis(12L) +
                TimeUnit.MINUTES.toMillis(30L) +
                TimeUnit.SECONDS.toMillis(20L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleTwo() {
        String expected = "1 year, 1 month, 5d 00:00:00";

        long ms = TimeUnit.DAYS.toMillis(400L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleThree() {
        String expected = "12:00:20";

        long ms = TimeUnit.HOURS.toMillis(12L) +
                TimeUnit.SECONDS.toMillis(20L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleFour() {
        String expected = "00:30:00";

        long ms = TimeUnit.MINUTES.toMillis(30L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleFive() {
        String expected = "00:00:20";

        long ms = TimeUnit.SECONDS.toMillis(20L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleZero() {
        String expected = "-";

        long ms = 0L;
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleOneSecond() {
        String expected = "00:00:01";

        long ms = TimeUnit.SECONDS.toMillis(1L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleOneMinute() {
        String expected = "00:01:00";

        long ms = TimeUnit.MINUTES.toMillis(1L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

}