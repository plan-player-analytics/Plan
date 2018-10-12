package com.djrapitops.plan.data.store.mutators;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.mocks.SystemMockUtil;

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
    }

    @Test
    @Ignore("Missing Formatter")
    public void formatTimeAmount() {
//        String expResult = "1s";
//        String result = timeAmountFormatter.apply(TimeAmount.SECOND.ms());
//
//        assertEquals(expResult, result);
    }

    @Test
    @Ignore("Missing Formatter")
    public void formatTimeAmountMonths() {
//        long time = TimeAmount.DAY.ms() * 40L;
//        assertEquals("1 month, 10d ", timeAmountFormatter.apply(time));
    }

}