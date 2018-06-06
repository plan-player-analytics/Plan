package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plugin.api.TimeAmount;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.Teardown;
import utilities.mocks.SystemMockUtil;

import static org.junit.Assert.assertEquals;

/**
 * Test for the Formatters class.
 *
 * @author Rsl1122
 */
public class FormattersTest {
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem();
        Teardown.resetSettingsTempValues();
    }

    @Test
    public void formatTimeAmount() {
        String expResult = "1s";
        String result = Formatters.timeAmount().apply(TimeAmount.SECOND.ms());

        assertEquals(expResult, result);
    }

    @Test
    public void formatTimeAmountMonths() {
        long time = TimeAmount.DAY.ms() * 40L;
        assertEquals("1 month, 10d ", Formatters.timeAmount().apply(time));
    }

}