package com.djrapitops.plan.utilities;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.RandomData;
import utilities.Teardown;
import utilities.mocks.SystemMockUtil;

import static org.junit.Assert.*;

/**
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class FormatUtilsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem();
        Teardown.resetSettingsTempValues();
    }

    @Test
    public void testMergeArrays() {
        String randomString1 = RandomData.randomString(10);
        String randomString2 = RandomData.randomString(10);
        String randomString3 = RandomData.randomString(10);
        String randomString4 = RandomData.randomString(10);

        String[][] arrays = new String[][]{new String[]{randomString1, randomString2}, new String[]{randomString3, randomString4}};
        String[] expResult = new String[]{randomString1, randomString2, randomString3, randomString4};

        String[] result = FormatUtils.mergeArrays(arrays);

        assertArrayEquals(expResult, result);
    }

    @Test
    public void testCutDecimalsWhichIsRoundedDown() {
        double d = 0.05234;

        String result = FormatUtils.cutDecimals(d);

        assertTrue("0.05".equals(result) || "0,05".equals(result));
    }

    @Test
    public void testCutDecimalsWhichIsRoundedUp() {
        double d = 0.05634;

        String result = FormatUtils.cutDecimals(d);

        assertTrue("0.06".equals(result) || "0,06".equals(result));
    }

    @Test
    public void testFormatIP() {
        String ip = "1.2.3.4";
        String ip2 = "1.2.3.26";
        String ip3 = "1.2.3.235";
        String expected = "1.2.xx.xx";

        assertEquals(expected, FormatUtils.formatIP(ip));
        assertEquals(expected, FormatUtils.formatIP(ip2));
        assertEquals(expected, FormatUtils.formatIP(ip3));
    }

}
