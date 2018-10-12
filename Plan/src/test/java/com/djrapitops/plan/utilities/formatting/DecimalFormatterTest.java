package com.djrapitops.plan.utilities.formatting;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test for {@link DecimalFormatter} class.
 *
 * @author Rsl1122
 */
public class DecimalFormatterTest {

    @Test
    @Ignore("Config missing")
    public void testCutDecimalsWhichIsRoundedDown() {
        double d = 0.05234;

        String result = new DecimalFormatter(null).apply(d);

        assertTrue("0.05".equals(result) || "0,05".equals(result));
    }

    @Test
    @Ignore("Config missing")
    public void testCutDecimalsWhichIsRoundedUp() {
        double d = 0.05634;

        String result = new DecimalFormatter(null).apply(d);

        assertTrue("0.06".equals(result) || "0,06".equals(result));
    }

}