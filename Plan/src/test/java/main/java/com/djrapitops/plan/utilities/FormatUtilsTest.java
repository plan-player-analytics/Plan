package test.java.main.java.com.djrapitops.plan.utilities;

import main.java.com.djrapitops.plan.utilities.FormatUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import java.util.Date;

import static org.junit.Assert.*;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class FormatUtilsTest {

    /**
     *
     */
    @Before
    public void setUp() {
    }

    /**
     *
     */
    public FormatUtilsTest() {
    }

    /**
     *
     */
    @Test
    public void testFormatTimeAmount() throws Exception {
        TestInit t = TestInit.init();
        long second = 1000L;
        String expResult = "1s";
        String result = FormatUtils.formatTimeAmount(second);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testFormatTimeAmountSinceDate() throws Exception {
        TestInit.init();
        Date before = new Date(300000L);
        Date now = new Date(310000L);
        String expResult = "10s";
        String result = FormatUtils.formatTimeAmountDifference(before.getTime(), now.getTime());
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testFormatTimeStamp() {
        long epochZero = 0L;
        String expResult = "Jan 01, 01:00";
        String result = FormatUtils.formatTimeStamp(epochZero);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testFormatTimeStampYear() {
        long epochZero = 0L;
        String expResult = "Jan 01 1970, 01:00";
        String result = FormatUtils.formatTimeStampYear(epochZero);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testRemoveLetters() {
        String dataPoint = "435729847jirggu.eiwb¤#¤%¤#";
        String expResult = "435729847.";
        String result = FormatUtils.removeLetters(dataPoint);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testRemoveNumbers() {
        String dataPoint = "34532453.5 $";
        String expResult = "$";
        String result = FormatUtils.removeNumbers(dataPoint);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testRemoveNumbers2() {
        String dataPoint = "l43r4545tl43  4.5";
        String expResult = "lrtl";
        String result = FormatUtils.removeNumbers(dataPoint);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testParseVersionNumber() {
        String versionString = "2.10.2";
        int expResult = 21002;
        int result = FormatUtils.parseVersionNumber(versionString);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testVersionNumber() {
        String versionString = "2.10.2";
        String versionString2 = "2.9.3";
        int result = FormatUtils.parseVersionNumber(versionString);
        int result2 = FormatUtils.parseVersionNumber(versionString2);
        assertTrue("Higher version not higher", result > result2);
    }

    /**
     *
     */
    @Test
    public void testMergeArrays() {
        String[][] arrays = new String[][]{new String[]{"Test", "One"}, new String[]{"Test", "Two"}};
        String[] expResult = new String[]{"Test", "One", "Test", "Two"};
        String[] result = FormatUtils.mergeArrays(arrays);
        assertArrayEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testFormatLocation() {
        World mockWorld = MockUtils.mockWorld();
        Location loc = new Location(mockWorld, 0, 0, 0);
        String expResult = "x 0 z 0 in World";
        String result = FormatUtils.formatLocation(loc);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testCutDecimals() throws Exception {
        TestInit.init();
        double d = 0.05234;
        String expResult = "0,05";
        String result = FormatUtils.cutDecimals(d);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testCutDecimals2() throws Exception {
        TestInit.init();
        double d = 0.05634;
        String expResult = "0,06";
        String result = FormatUtils.cutDecimals(d);
        assertEquals(expResult, result);
    }

}
