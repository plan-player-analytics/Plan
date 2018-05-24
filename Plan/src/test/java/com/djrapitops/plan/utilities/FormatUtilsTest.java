package com.djrapitops.plan.utilities;

import com.djrapitops.plugin.api.TimeAmount;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.RandomData;
import utilities.Teardown;
import utilities.mocks.SystemMockUtil;
import utilities.mocks.objects.MockUtils;

import java.util.Date;

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
    public void testFormatTimeAmount() {
        String expResult = "1s";
        String result = FormatUtils.formatTimeAmount(TimeAmount.SECOND.ms());

        assertEquals(expResult, result);
    }

    @Test
    public void testFormatTimeAmountSinceDate() {
        Date before = new Date(300000L);
        Date now = new Date(310000L);

        String expResult = "10s";
        String result = FormatUtils.formatTimeAmountDifference(before.getTime(), now.getTime());

        assertEquals(expResult, result);
    }

    @Test
    public void testFormatTimeAmountMonths() {
        long time = TimeAmount.DAY.ms() * 40L;
        assertEquals("1 month, 10d ", FormatUtils.formatTimeAmount(time));
    }

    @Test
    public void testRemoveLetters() {
        String dataPoint = "435729847jirggu.eiwb¤#¤%¤#";
        String expResult = "435729847.";

        String result = FormatUtils.removeLetters(dataPoint);

        assertEquals(expResult, result);
    }

    @Test
    public void testRemoveNumbers() {
        String dataPoint = "34532453.5 $";
        String expResult = "$";

        String result = FormatUtils.removeNumbers(dataPoint);

        assertEquals(expResult, result);
    }

    @Test
    public void testRemoveNumbers2() {
        String dataPoint = "l43r4545tl43  4.5";
        String expResult = "lrtl";

        String result = FormatUtils.removeNumbers(dataPoint);

        assertEquals(expResult, result);
    }

    @Test
    public void testParseVersionNumber() {
        String versionString = "2.10.2";
        long expResult = 21002000000000000L;

        long result = FormatUtils.parseVersionNumber(versionString);

        assertEquals(expResult, result);
    }

    @Test
    public void testVersionNumber() {
        String versionString = "2.10.2";
        String versionString2 = "2.9.3";

        long result = FormatUtils.parseVersionNumber(versionString);
        long result2 = FormatUtils.parseVersionNumber(versionString2);

        assertTrue("Higher version not higher", result > result2);
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
    public void testFormatLocation() {
        int randomInt = RandomData.randomInt(0, 100);

        World mockWorld = MockUtils.mockWorld();
        Location loc = new Location(mockWorld, randomInt, randomInt, randomInt);

        String expResult = "x " + randomInt + " z " + randomInt + " in World";
        String result = FormatUtils.formatLocation(loc);

        assertEquals(expResult, result);
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
