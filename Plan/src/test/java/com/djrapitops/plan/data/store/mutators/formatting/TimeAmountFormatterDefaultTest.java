package com.djrapitops.plan.data.store.mutators.formatting;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.TimeAmount;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.Teardown;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link TimeAmountFormatter} that checks extra zeros config example.
 *
 * @author Rsl1122
 */
public class TimeAmountFormatterDefaultTest {

    private TimeAmountFormatter timeAmountFormatter;

    @BeforeClass
    public static void setUpClass() {
        Settings.FORMAT_YEAR.setTemporaryValue("1 year, ");
        Settings.FORMAT_YEARS.setTemporaryValue("%years% years, ");
        Settings.FORMAT_MONTH.setTemporaryValue("1 month, ");
        Settings.FORMAT_MONTHS.setTemporaryValue("%months% months, ");
        Settings.FORMAT_DAY.setTemporaryValue("1d ");
        Settings.FORMAT_DAYS.setTemporaryValue("%days%d ");
        Settings.FORMAT_HOURS.setTemporaryValue("%hours%h ");
        Settings.FORMAT_MINUTES.setTemporaryValue("%minutes%m ");
        Settings.FORMAT_SECONDS.setTemporaryValue("%seconds%s");
        Settings.FORMAT_ZERO_SECONDS.setTemporaryValue("0s");
    }

    @AfterClass
    public static void tearDownClass() {
        Teardown.resetSettingsTempValues();
    }

    @Before
    public void setUp() {
        timeAmountFormatter = new TimeAmountFormatter();
    }

    @Test
    public void exampleOne() {
        String expected = "1 year, 1 month, 5d 12h 30m 20s";

        long ms = TimeAmount.DAY.ms() * 400L +
                TimeAmount.HOUR.ms() * 12L +
                TimeAmount.MINUTE.ms() * 30L +
                TimeAmount.SECOND.ms() * 20L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleTwo() {
        String expected = "1 year, 1 month, 5d ";

        long ms = TimeAmount.DAY.ms() * 400L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleThree() {
        String expected = "12h 20s";

        long ms = TimeAmount.HOUR.ms() * 12L +
                TimeAmount.SECOND.ms() * 20L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleFour() {
        String expected = "30m ";

        long ms = TimeAmount.MINUTE.ms() * 30L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleFive() {
        String expected = "20s";

        long ms = TimeAmount.SECOND.ms() * 20L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleZero() {
        String expected = "-";

        long ms = 0L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleOneSecond() {
        String expected = "1s";

        long ms = TimeAmount.SECOND.ms();
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleOneMinute() {
        String expected = "1m ";

        long ms = TimeAmount.MINUTE.ms();
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

}