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
public class TimeAmountFormatterExtraZerosTest {

    private TimeAmountFormatter timeAmountFormatter;

    @BeforeClass
    public static void setUpClass() {
        Settings.FORMAT_YEAR.setTemporaryValue("1 year, ");
        Settings.FORMAT_YEARS.setTemporaryValue("%years% years, ");
        Settings.FORMAT_MONTH.setTemporaryValue("1 month, ");
        Settings.FORMAT_MONTHS.setTemporaryValue("%months% months, ");
        Settings.FORMAT_DAY.setTemporaryValue("1d ");
        Settings.FORMAT_DAYS.setTemporaryValue("%days%d ");
        Settings.FORMAT_HOURS.setTemporaryValue("%zero%%hours%:");
        Settings.FORMAT_MINUTES.setTemporaryValue("%hours%%zero%%minutes%:");
        Settings.FORMAT_SECONDS.setTemporaryValue("%minutes%%zero%%seconds%");
        Settings.FORMAT_ZERO_SECONDS.setTemporaryValue("00:00:00");
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
        String expected = "1 year, 1 month, 5d 12:30:20";

        long ms = TimeAmount.DAY.ms() * 400L +
                TimeAmount.HOUR.ms() * 12L +
                TimeAmount.MINUTE.ms() * 30L +
                TimeAmount.SECOND.ms() * 20L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleTwo() {
        String expected = "1 year, 1 month, 5d 00:00:00";

        long ms = TimeAmount.DAY.ms() * 400L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleThree() {
        String expected = "12:00:20";

        long ms = TimeAmount.HOUR.ms() * 12L +
                TimeAmount.SECOND.ms() * 20L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleFour() {
        String expected = "00:30:00";

        long ms = TimeAmount.MINUTE.ms() * 30L;
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleFive() {
        String expected = "00:00:20";

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
        String expected = "00:00:01";

        long ms = TimeAmount.SECOND.ms();
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

    @Test
    public void exampleOneMinute() {
        String expected = "00:01:00";

        long ms = TimeAmount.MINUTE.ms();
        String result = timeAmountFormatter.apply(ms);

        assertEquals(expected, result);
    }

}