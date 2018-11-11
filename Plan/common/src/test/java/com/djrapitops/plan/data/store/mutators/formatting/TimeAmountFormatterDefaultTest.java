package com.djrapitops.plan.data.store.mutators.formatting;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
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
public class TimeAmountFormatterDefaultTest {

    private static TimeAmountFormatter underTest;

    @BeforeClass
    public static void setUpClass() {
        PlanConfig config = Mockito.mock(PlanConfig.class);
        when(config.getString(Settings.FORMAT_YEAR)).thenReturn("1 year, ");
        when(config.getString(Settings.FORMAT_YEARS)).thenReturn("%years% years, ");
        when(config.getString(Settings.FORMAT_MONTH)).thenReturn("1 month, ");
        when(config.getString(Settings.FORMAT_YEAR)).thenReturn("1 year, ");
        when(config.getString(Settings.FORMAT_MONTH)).thenReturn("1 month, ");
        when(config.getString(Settings.FORMAT_MONTHS)).thenReturn("%months% months, ");
        when(config.getString(Settings.FORMAT_DAY)).thenReturn("1d ");
        when(config.getString(Settings.FORMAT_DAYS)).thenReturn("%days%d ");
        when(config.getString(Settings.FORMAT_HOURS)).thenReturn("%hours%h ");
        when(config.getString(Settings.FORMAT_MINUTES)).thenReturn("%minutes%m ");
        when(config.getString(Settings.FORMAT_SECONDS)).thenReturn("%seconds%s");
        when(config.getString(Settings.FORMAT_ZERO_SECONDS)).thenReturn("0s");
        underTest = new TimeAmountFormatter(config);
    }

    @Test
    public void exampleOne() {
        String expected = "1 year, 1 month, 5d 12h 30m 20s";

        long ms = TimeUnit.DAYS.toMillis(400L) +
                TimeUnit.HOURS.toMillis(12L) +
                TimeUnit.MINUTES.toMillis(30L) +
                TimeUnit.SECONDS.toMillis(20L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleTwo() {
        String expected = "1 year, 1 month, 5d ";

        long ms = TimeUnit.DAYS.toMillis(400L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleThree() {
        String expected = "12h 20s";

        long ms = TimeUnit.HOURS.toMillis(12L) +
                TimeUnit.SECONDS.toMillis(20L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleFour() {
        String expected = "30m ";

        long ms = TimeUnit.MINUTES.toMillis(30L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleFive() {
        String expected = "20s";

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
        String expected = "1s";

        long ms = TimeUnit.SECONDS.toMillis(1L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleOneMinute() {
        String expected = "1m ";

        long ms = TimeUnit.MINUTES.toMillis(1L);
        String result = underTest.apply(ms);

        assertEquals(expected, result);
    }

}