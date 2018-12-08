package com.djrapitops.plan.data.store.mutators.formatting;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.FormatSettings;
import com.djrapitops.plan.utilities.formatting.DecimalFormatter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DecimalFormatter} class.
 *
 * @author Rsl1122
 */
public class DecimalFormatterTest {

    private DecimalFormatter underTest;

    @Before
    public void setUp() {
        PlanConfig config = Mockito.mock(PlanConfig.class);
        when(config.getString(FormatSettings.DECIMALS)).thenReturn("#.##");

        underTest = new DecimalFormatter(config);
    }

    @Test
    public void testCutDecimalsWhichIsRoundedDown() {
        double d = 0.05234;

        String result = underTest.apply(d);

        assertTrue("0.05".equals(result) || "0,05".equals(result));
    }

    @Test
    public void testCutDecimalsWhichIsRoundedUp() {
        double d = 0.05634;

        String result = underTest.apply(d);

        assertTrue("0.06".equals(result) || "0,06".equals(result));
    }

}